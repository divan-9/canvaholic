(ns canvaholic.rendering
  (:require
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

(defn- generate-nodes
  [nodes]
  (map generate-node nodes))

(defn- generate-svg
  [canvas]
  [:svg
   {:viewbox (str/join " " (generate-viewbox canvas))
    :width (get-in canvas [:boundaries :width])
    :height (get-in canvas [:boundaries :height])
    :xmlns "http://www.w3.org/2000/svg"}
   (list
    [:style "
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

     .mode-canvas-color-1 {
        --canvas-color: var(--canvas-color-1);
        fill: var(--canvas-color);
     }

     .mode-canvas-color-2 {
        --canvas-color: var(--canvas-color-2);
        fill: var(--canvas-color);
     }

     .mode-canvas-color-3 {
        --canvas-color: var(--canvas-color-3);
        fill: var(--canvas-color);
     }

     .mode-canvas-color-4 {
        --canvas-color: var(--canvas-color-4);
        fill: var(--canvas-color);
     }

     .mode-canvas-color-5 {
        --canvas-color: var(--canvas-color-5);
        fill: var(--canvas-color);
     }

     .mode-canvas-color-6 {
        --canvas-color: var(--canvas-color-6);
        fill: var(--canvas-color);
     }

     .node {
        stroke-width: 2px;
        rx: 5px;
        stroke: var(--canvas-color);
        fill-opacity: 0.1;
     }

     div.node-content {
        background: transparent;
        padding-left: 24px;
        padding-right: 24px;
        line-height: 22.5px;
        font-size: 18px;
        color: rgb(38, 38, 38);
        padding: 10px;
     }

     .canvas-group-label {
        font-size: 22.5px;
        fill: black;
     }

     "]
    (generate-nodes (:nodes canvas)))])

(defn render-svg
  [canvas]
  (->
   canvas
   (generate-svg)
   (h/html)
   (str)))
