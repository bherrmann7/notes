(ns notes.utils)

(defn mk-link [request & more]
  (clojure.string/join "" (cons (:context-path request) more))
  )

