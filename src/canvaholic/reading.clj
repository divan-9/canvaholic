(ns canvaholic.reading
  (:require
   [clojure.data.json :as json]))

(def ObsidianNode
  [:map
   ["id" :string]
   ["type" [:enum "group" "text" "file"]]
   ["label" {:optional true} :string]
   ["text" {:optional true} :string]
   ["color" {:optional true} [:enum "1" "2" "3" "4" "5" "6"]]
   ["x" :int]
   ["y" :int]
   ["width" :int]
   ["height" :int]])

(def ObsidianEdge
  [:map
   ["id" :string]
   ["fromNode" :string]
   ["toNode" :string]
   ["fromSide" [:enum "top" "bottom" "left" "right"]]
   ["toSide" [:enum "top" "bottom" "left" "right"]]
   ["color" {:optional true} [:enum "1" "2" "3" "4" "5" "6"]]])

(def ObsidianCanvas
  [:map
   ["edges" [:vector ObsidianEdge]]
   ["nodes" [:vector ObsidianNode]]])

(defn deserialize
  "Deserialize the given string into a canvas."
  {:malli/schema [:=> [:cat :string] ObsidianCanvas]}
  [canvas-string]
  (json/read-str canvas-string))
