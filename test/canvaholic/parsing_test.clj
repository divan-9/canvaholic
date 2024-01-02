(ns canvaholic.parsing-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.data.json :as json]
            [canvaholic.parsing :refer [parse-canvas]]))

(def empty-canvas-str "{}")

(def simple-canvas-str
  (json/write-str
   {:nodes [{:id "1" :type "group" :label "group label" :x 10 :y 20 :width 30 :height 40}
            {:id "2" :type "text" :text "text content" :x 50 :y 60 :width 70 :height 80}]
    :edges []}))

(deftest empty-canvas
  (let [input "{}"
        res (parse-canvas input)
        empty-boundaries {:x 0 :y 0 :width 0 :height 0}]
    (is (= 0 (count (:nodes res))) "empty canvas has no nodes")
    (is (= empty-boundaries (:boundaries res)) "empty canvas has empty boundaries")))

(deftest simple-canvas
  (let [res (parse-canvas simple-canvas-str)
        boundaries {:x 0 :y 0 :width 110 :height 120}]
    (is (= 2 (count (:nodes res))) "simple canvas has two nodes")
    (is (= boundaries (:boundaries res)) "simple canvas has correct boundaries")))
