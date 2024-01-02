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

(defn- generate-node
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
        font-family: monospace;
        color: rgb(38, 38, 38);
        padding: 10px;
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
