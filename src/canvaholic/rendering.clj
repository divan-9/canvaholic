(ns canvaholic.rendering
  (:require
   [clojure.math :refer [round]]
   [hiccup2.core :as h]
   [clojure.string :as str]))

(defn- generate-viewbox
  [canvas]
  [(get-in canvas [:boundaries :x])
   (get-in canvas [:boundaries :y])
   (get-in canvas [:boundaries :width])
   (get-in canvas [:boundaries :height])])

(defmulti generate-node :type)

(defmethod generate-node "file"
  [node]
  nil)

(defmethod generate-node "group"
  [node]
  (list
   [:rect {:x (:x node)
           :y (:y node)
           :id (:id node)
           :class "node"
           :width (:width node)
           :height (:height node)}]
   [:text
    {:x (+ (:x node) 4)
     :y (- (:y node) 8)
     :class "canvas-group-label"}
    (:label node)]))

(defmethod generate-node "text"
  [node]
  (list
   [:rect {:x (:x node)
           :y (:y node)
           :id (:id node)
           :class ["node" (str "mode-canvas-color-" (:color node))]
           :width (:width node)
           :height (:height node)}]
   [:foreignObject
    {:x (:x node)
     :y (:y node)
     :width (:width node)
     :height (:height node)}
    [:div.node-content
     {:xmlns "http://www.w3.org/1999/xhtml"}
     [:pre (:text node)]]]))

(defn- generate-nodes-svg
  [{:keys [nodes]}]
  (map generate-node nodes))

(def style "
     svg {
        font-family: monospace;
        fill: transparent;
        --canvas-color: rgb(192,192,192);
        --color-red-rgb: rgb(233, 49, 71);
        --color-orange-rgb: rgb(236, 117, 0);
        --color-yellow-rgb: rgb(224, 172, 0);
        --color-green-rgb: rgb(8, 185, 78);
        --color-cyan-rgb: rgb(0, 191, 188);
        --color-blue-rgb: rgb(8, 109, 221);
        --color-purple-rgb: rgb(120, 82, 238);
        --color-pink-rgb: rgb(213, 57, 132);
        --canvas-color-1: var(--color-red-rgb);
        --canvas-color-2: var(--color-orange-rgb);
        --canvas-color-3: var(--color-yellow-rgb);
        --canvas-color-4: var(--color-green-rgb);
        --canvas-color-5: var(--color-cyan-rgb);
        --canvas-color-6: var(--color-purple-rgb);
     }

     pre {
       white-space: pre-wrap;
     }

     .mode-canvas-color-1 {
        --canvas-color: var(--canvas-color-1);
     }

     .mode-canvas-color-2 {
        --canvas-color: var(--canvas-color-2);
     }

     .mode-canvas-color-3 {
        --canvas-color: var(--canvas-color-3);
     }

     .mode-canvas-color-4 {
        --canvas-color: var(--canvas-color-4);
     }

     .mode-canvas-color-5 {
        --canvas-color: var(--canvas-color-5);
     }

     .mode-canvas-color-6 {
        --canvas-color: var(--canvas-color-6);
     }

     .node {
        stroke-width: 2.5px;
        rx: 5px;
        stroke: var(--canvas-color);
        fill: var(--canvas-color);
        fill-opacity: 0.1;
     }

     .edge {
        stroke-width: 2.5px;
        stroke: var(--canvas-color);
        fill: transparent;
      }

     .edge-pointer {
        fill: var(--canvas-color);
      }

     div.node-content {
        background: transparent;
        padding: 10px;
        padding-left: 24px;
        padding-right: 24px;
        font-size: 18px;
        color: rgb(38, 38, 38);
        margin-top: -10px;
     }

     .canvas-group-label {
        font-size: 22.5px;
        fill: black;
     }

     ");

(defn path
  [from-x from-y to-x to-y]
  ["M"
   (str from-x " " from-y)
   "C"
   (str (+ from-x 150) " " from-y)
   (str (- to-x 150) " " to-y)
   (str to-x " " to-y)])

(defmulti side-center
  "Accepts side and node and returns the center position of the side"
  (fn [side _] side))

(defmethod side-center "left"
  [_ {:keys [x y height]}]
  [x
   (round (+ y (/ height 2)))])

(defmethod side-center "right"
  [_ {:keys [x y width height]}]
  [(+ x width)
   (round (+ y (/ height 2)))])

(defmethod side-center "top"
  [_ {:keys [x y width]}]
  [(round (+ x (/ width 2)))
   y])

(defmethod side-center "bottom"
  [_ {:keys [x y width height]}]
  [(round (+ x (/ width 2)))
   (+ y height)])

(defn control-point
  [[x y] side]
  (case side
    "left" [(- x 120) y]
    "right" [(+ x 120) y]
    "top" [x (- y 120)]
    "bottom" [x (+ y 120)]))

(defn path-definition
  [from-pos from-side to-pos to-side]
  (let
   [[from-x from-y] from-pos
    [to-x to-y] to-pos
    [c-from-x c-from-y] (control-point from-pos from-side)
    [c-to-x c-to-y] (control-point to-pos to-side)]
    ["M"
     from-x from-y
     "C"
     c-from-x c-from-y
     c-to-x c-to-y
     to-x to-y]))

(defn path-pointer
  [to-pos to-side]
  (let
   [[x y] to-pos]
    [(str x "," y)
     (str (+ x 6.5) "," (+ y 10.4))
     (str (- x 6.5) "," (+ y 10.4))]))

(defn angle [side]
  (case side
    "left" 90
    "right" -90
    "top" 180
    "bottom" 0))

(defn generate-edge-svg
  [node-map edge]
  (let
   [{:keys [id fromSide toSide fromNode toNode]} edge
    from-node (node-map fromNode)
    from (side-center fromSide from-node)
    to (side-center toSide (node-map toNode))]
    (list
     [:path
      {:d (str/join " " (path-definition from  fromSide to toSide))
       :id id
       :class ["edge" (str "mode-canvas-color-" (:color edge))]}]
     [:polygon
      {:points "0,0 6.5,10.4 -6.5,10.4"
       :transform (format
                   "translate(%s,%s) rotate(%s)"
                   (first to)
                   (second to)
                   (angle toSide))
       :class ["edge-pointer" (str "mode-canvas-color-" (:color edge))]}])))

(defn- generate-edges-svg
  [{:keys [nodes edges]}]
  (let
   [node-map (into {} (map (fn [x] [(:id x) x]) nodes))]
    (map (partial generate-edge-svg node-map) edges)))

(defn- generate-svg
  [canvas]
  [:svg
   {:width (get-in canvas [:boundaries :width])
    :height (get-in canvas [:boundaries :height])
    :xmlns "http://www.w3.org/2000/svg"}
   (list
    [:style style]
    (generate-edges-svg canvas)
    (generate-nodes-svg canvas))])

(defn render-svg
  [canvas]
  (->
   canvas
   (generate-svg)
   (h/html)
   (str)))

(comment
  (into {} (map (fn [x] [(:id x) x]) [{:id "1"} {:id "2"}]))
  (round (/ 56963 2))

 ; 
  )
