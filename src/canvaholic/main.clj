(ns canvaholic.main
  (:require
   [canvaholic.rendering :refer [render-svg]]
   [canvaholic.parsing :refer [parse-canvas]]))

(defn canvas-to-svg
  "Generate SVG from the given data."
  [canvas-str]
  (render-svg (parse-canvas canvas-str)))

(defn -main
  [& args]
  (if (> (.available System/in) 0)
    (println (canvas-to-svg (slurp *in*)))
    (println "The program is supposed to be called in pipe. Try 'cat file | canvaholic'")))

(comment
  (let
   [edges (:edges (parse-canvas (slurp "/Users/dmitryivanov/dev/mind/Demo.canvas")))]
    edges)
  ;
  )
