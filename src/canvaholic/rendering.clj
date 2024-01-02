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
           :class "node"
           :width (:width node)
           :height (:height node)}]
   [:foreignObject
    {:x (:x node)
     :y (:y node)
     :width (:width node)
     :height (:height node)}
    [:div.node-content
     {:xmlns "http://www.w3.org/1999/xhtml"}
     (:text node)]]))

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
     }

     rect.node {
        fill: transparent;
        stroke: rgb(192,192,192);
        stroke-width: 2px;
        rx: 5px;
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
