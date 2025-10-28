(ns com.prithvianilk.parser-options)

(defrecord ParserOptions [omit-media])

(defn create-options
  "Create parser options with optional omit-media flag"
  ([]
   (create-options false))
  ([omit-media]
   (->ParserOptions omit-media)))
