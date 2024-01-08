(ns canvaholic.rendering
  (:require
   [hiccup2.core :as h]
   [clojure.string :as str]))

(defn- generate-group-label
  [node]
  [:text {:x (+ (get-in node [:rect :x]) 4)
          :y (-  (get-in node [:rect :y]) 8)
          :font-size "22.5px"
          :fill "black"}
   (:label node)])

(defn- generate-group-rect
  [node]
  [:rect {:id (:id node)
          :x (get-in node [:rect :x])
          :y (get-in node [:rect :y])
          :width (get-in node [:rect :width])
          :height (get-in node [:rect :height])
          :stroke (:color node)
          :stroke-width "2.5"
          :rx "5"
          :fill (:color node)
          :fill-opacity "0.1"}])

(defn- generate-text-rect
  [node]
  [:rect
   {:id (:id node)
    :x (get-in node [:rect :x])
    :y (get-in node [:rect :y])
    :width (get-in node [:rect :width])
    :height (get-in node [:rect :height])
    :stroke (:color node)
    :stroke-width "2.5"
    :rx "5"
    :fill (:color node)
    :fill-opacity "0.1"}])

(defn- generate-text-content
  [node]
  [:foreignObject {:x (get-in node [:rect :x])
                   :y (get-in node [:rect :y])
                   :width (get-in node [:rect :width])
                   :height (get-in node [:rect :height])}
   [:div {:xmlns "http://www.w3.org/1999/xhtml"
          :style {:padding "10px 24px 10px 24px"
                  :background "transparent"
                  :font-size "15px"
                  :font-family "monospace"
                  :margin-top "-10px"}}
    [:pre
     {:style {:white-space "pre-wrap"}}
     (:text node)]]])

(defmulti generate-node :type)

(defmethod generate-node :group
  [node]
  (list
   (generate-group-label node)
   (generate-group-rect node)))

(defmethod generate-node :text
  [node]
  (list
   (generate-text-rect node)
   (generate-text-content node)))

(defn- render-edge-path [path]
  (let [vs (map vals path)]
    (apply format
           "M%s,%s C%s,%s %s,%s %s,%s"
           (flatten vs))))

(defn- render-edge-pointer
  [edge]
  (let [target (nth (:path edge) 3)]
    [:polygon
     {:points "0,0 6.5,10.4 -6.5,10.4"
      :transform (format
                  "translate(%s,%s) rotate(%s)"
                  (:x target)
                  (:y target)
                  (:pointer-angle edge))
      :fill (:color edge)}]))

(defn- render-edge-curve
  [edge]
  [:path
   {:d (render-edge-path (:path edge))
    :id (:id edge)
    :stroke (:color edge)
    :fill "transparent"
    :stroke-width "2.5"}])

(defn- render-edge-label
  [edge]
  (let
   [text (get-in edge [:label :text])
    lines (str/split text #"\n")
    y (get-in edge [:label :pos :y])
    x (get-in edge [:label :pos :x])]
    [:text
     {:x x
      :y (- y 24)
      :font-size "15"
      :dominant-baseline "middle"
      :text-anchor "middle"}
     (map (fn
            [line]
            [:tspan
             {:x x
              :dy "1.4em"}
             line])
          lines)]))

(defn- generate-edge-svg
  [edge]
  [:g
   (render-edge-curve edge)
   (render-edge-pointer edge)
   (render-edge-label edge)])

(defn- generate-edges-svg [edges]
  (map generate-edge-svg edges))

(defn- generate-nodes-svg [nodes]
  (map generate-node nodes))

(defn generate-hiccup
  [canvas]
  [:svg
   {:width (:width canvas)
    :height (:height canvas)
    :font-family "monospace"
    :xmlns "http://www.w3.org/2000/svg"}
   (generate-nodes-svg (:nodes canvas))
   (generate-edges-svg (:edges canvas))])

(defn render-svg
  [canvas]
  (-> canvas
      (generate-hiccup)
      (h/html)
      (str)))
