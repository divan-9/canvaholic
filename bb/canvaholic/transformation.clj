(ns canvaholic.transformation
  (:require
   [clojure.math :refer [round]]
   [canvaholic.reading :refer [deserialize ObsidianCanvas]]))

(def Color
  [:enum
   "#c0c0c0" ; default
   "#e93147" ; 1
   "#ec7500" ; 2
   "#e0ac00" ; 3
   "#08b94f" ; 4
   "#00bfbc" ; 5
   "#086ddd" ; ?
   "#7952ee" ; 6
   "#d53984" ; ?
   ])

(def Rectangle
  [:map
   [:x nat-int?]
   [:y nat-int?]
   [:width nat-int?]
   [:height nat-int?]])

(def CanvasNodeGroup
  [:map
   [:id string?]
   [:rect Rectangle]
   [:type [:enum :group]]
   [:color Color]
   [:label string?]])

(def CanvasNodeText
  [:map
   [:id string?]
   [:rect Rectangle]
   [:type [:enum :text]]
   [:color Color]
   [:text string?]])

(def CanvasNode
  [:or CanvasNodeGroup CanvasNodeText])

(def Pos
  [:map
   [:x nat-int?]
   [:y nat-int?]])

(def CanvasEdge
  [:map {:closed true}
   [:id string?]
   [:label
    [:map
     :text string?
     :pos Pos]]
   [:path [:tuple Pos Pos Pos Pos]]
   [:pointer-angle [:enum -90 0 90 180]]
   [:color Color]])

(def Canvas
  [:map
   [:width nat-int?]
   [:height nat-int?]
   [:nodes [:sequential CanvasNode]]
   [:edges [:sequential CanvasEdge]]])

(defn- get-minmax [nodes]
  (if
   (empty? nodes) {:min-x 0 :min-y 0 :max-x 0 :max-y 0}
   (reduce
    (fn [acc {x "x" y "y" width "width" height "height"}]
      (-> acc
          (update :min-x (partial min (- x 300)))
          (update :min-y (partial min (- y 300)))
          (update :max-x (partial max (+ x width)))
          (update :max-y (partial max (+ y height)))))
    {:min-x Integer/MAX_VALUE
     :min-y Integer/MAX_VALUE
     :max-x Integer/MIN_VALUE
     :max-y Integer/MIN_VALUE}
    nodes)))

(def color-map
  {"1" "#e93147"
   "2" "#ec7500"
   "3" "#e0ac00"
   "4" "#08b94f"
   "5" "#00bfbc"
   "6" "#7952ee"})

(defmulti convert-node #(% "type"))

(defmethod convert-node "group" [raw]
  {:rect
   {:x (raw "x")
    :y (raw "y")
    :width (raw "width")
    :height (raw "height")}
   :type :group
   :color (color-map (raw "color") "#c0c0c0")
   :id (raw "id")
   :label (raw "label")})

(defmethod convert-node "text" [raw]
  {:rect
   {:x (raw "x")
    :y (raw "y")
    :width (raw "width")
    :height (raw "height")}
   :type :text
   :color (color-map (raw "color") "#c0c0c0")
   :id (raw "id")
   :text (raw "text")})

(defmethod convert-node :default [raw]
  {:rect
   {:x (raw "x")
    :y (raw "y")
    :width (raw "width")
    :height (raw "height")}
   :type :text
   :color (color-map (raw "color") "#c0c0c0")
   :id (raw "id")
   :text "NOT SUPPORTED"})

(defn- offset-node
  "Offset nodes by min-x and min-y"
  [{:keys [min-x min-y]} el]
  (-> el
      (update-in [:rect :x] #(- % min-x))
      (update-in [:rect :y] #(- % min-y))))

(defn- convert-nodes
  [raw-nodes]
  (->> raw-nodes
       (map #(convert-node %))))

(defmulti side-center
  "Accepts side and node and returns the center position of the side"
  (fn [side _] side))

(defmethod side-center "left"
  [_ {:keys [x y height]}]
  {:x x :y (round (+ y (/ height 2)))})

(defmethod side-center "right"
  [_ {:keys [x y width height]}]
  {:x (+ x width)
   :y (round (+ y (/ height 2)))})

(defmethod side-center "top"
  [_ {:keys [x y width]}]
  {:x (round (+ x (/ width 2)))
   :y y})

(defmethod side-center "bottom"
  [_ {:keys [x y width height]}]
  {:x (round (+ x (/ width 2)))
   :y (+ y height)})

(defn control-point
  [side {:keys [x y]}]
  (case side
    "left" {:x (- x 120) :y y}
    "right" {:x (+ x 120) :y y}
    "top" {:x x :y (- y 120)}
    "bottom" {:x x :y (+ y 120)}))

(defn- get-path
  [from-rect to-rect from-side to-side]
  [(side-center from-side from-rect)
   (control-point from-side (side-center from-side from-rect))
   (control-point to-side (side-center to-side to-rect))
   (side-center to-side to-rect)])

(defn- get-label-pos
  "Position of the edge label. It should be the center point of the edge curve"
  [from-rect to-rect from-side to-side]
  (let [cp1 (control-point from-side (side-center from-side from-rect))
        cp2 (control-point to-side (side-center to-side to-rect))]
    {:x (/ (+ (:x cp1) (:x cp2)) 2)
     :y (/ (+ (:y cp1) (:y cp2)) 2)}))

(defn- get-pointer-angle [to-side]
  (case to-side
    "left" 90
    "top" 180
    "bottom" 0
    "right" -90))

(defn- convert-edges
  [raw-edges node-map]
  (map
   (fn [el]
     (let
      [from-node (node-map (el "fromNode"))
       from-rect (:rect from-node)
       to-node (node-map (el "toNode"))
       to-rect (:rect to-node)
       from-side (el "fromSide")
       to-side (el "toSide")]
       {:pointer-angle (get-pointer-angle to-side)
        :path (get-path from-rect to-rect from-side to-side)
        :color (color-map (el "color") "#c0c0c0")
        :label
        {:pos (get-label-pos from-rect to-rect from-side to-side)
         :text (el "label" "")}
        :id (el "id")}))
   raw-edges))

(defn transform-canvas
  [raw]
  {:malli/schema [:=> ObsidianCanvas Canvas]}
  (let [minmax (get-minmax (raw "nodes"))
        {:keys [min-x min-y max-x max-y]} minmax
        nodes (convert-nodes (raw "nodes"))
        nodes (map #(offset-node minmax %) nodes)
        node-map (into {} (map (fn [x] [(:id x) x]) nodes))
        edges (convert-edges (raw "edges") node-map)]
    {:minmax minmax
     :width (- max-x min-x)
     :height (- max-y min-y)
     :nodes nodes
     :edges edges}))

