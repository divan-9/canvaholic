(ns canvaholic.main
  (:require
   [canvaholic.rendering :refer [render-svg]]
   [canvaholic.reading :refer [deserialize]]
   [canvaholic.transformation :refer [transform-canvas]]))

(defn canvas-to-svg
  "Generate SVG from the given data."
  [canvas-str]
  (-> canvas-str
      (deserialize)
      (transform-canvas)
      (render-svg)))

(defn -main
  [& _]
  (if (> (.available System/in) 0)
    (println (canvas-to-svg (slurp *in*)))
    (println "The program is supposed to be called in pipe. Try 'cat file | canvaholic'")))
