(ns canvaholic.parsing
  (:require
   [clojure.data.json :as json]))

(defn- get-minmax [nodes]
  (reduce
   (fn [acc {:keys [x y width height]}]
     (-> acc
         (update :min-x (partial min x))
         (update :min-y (partial min y))
         (update :max-x (partial max (+ x width)))
         (update :max-y (partial max (+ y height)))))
   {:min-x Integer/MAX_VALUE
    :min-y Integer/MAX_VALUE
    :max-x Integer/MIN_VALUE
    :max-y Integer/MIN_VALUE}
   nodes))

(defn- get-boundaries
  "Get the boundaries of the given canvas."
  [nodes]
  (if
   (empty? nodes) {:x 0 :y 0 :width 0 :height 0}
   (let [minmax (get-minmax nodes)]
     {:x (:min-x minmax)
      :y (:min-y minmax)
      :width (- (:max-x minmax) (:min-x minmax))
      :height (- (:max-y minmax) (:min-y minmax))})))

(defn- convert-nodes
  [raw-nodes]
  (map
   (fn [el]
     {:x (el "x")
      :y (el "y")
      :type (el "type")
      :text (el "text")
      :width (el "width")
      :color (el "color")
      :raw el
      :id (el "id")
      :label (el "label")
      :height (el "height")})
   raw-nodes))

(defn- transform
  "Transform el by applying f to each key in d"
  [el d f]
  (reduce
   (fn [acc [k v]] (update acc k f v))
   el
   d))

(defn- normalize
  [canvas]
  (let
   [boundaries (:boundaries canvas)
    nodes (:nodes canvas)
    diff (select-keys boundaries [:x :y])]
    (-> canvas
        (assoc :boundaries (transform boundaries diff -))
        (assoc :nodes (map #(transform % diff -) nodes)))))

(defn parse-canvas
  "Parse the given string into a canvas."
  [canvas-string]
  (let [raw (json/read-str canvas-string)
        raw-nodes (raw "nodes")
        nodes (convert-nodes raw-nodes)
        boundaries (get-boundaries nodes)
        canvas {:boundaries boundaries
                :edges (raw "edges")
                :nodes nodes}]
    (normalize canvas)))

(comment
  (get-boundaries nil)
  (get-boundaries [{:x -1 :y -100 :width 10 :height 10}]))
