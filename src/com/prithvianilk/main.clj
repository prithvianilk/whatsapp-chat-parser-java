(ns com.prithvianilk.main
  (:require [com.prithvianilk.whatsapp-chat-parser :refer [get-messages]])
  (:gen-class))

(defn -main
  "Main entry point for the WhatsApp chat parser"
  [& args]
  (if-let [file-path (first args)]
    (do
      (println (str "Parsing WhatsApp chat from: " file-path))
      (doseq [msg (get-messages file-path)]
        (println msg)))
    (println "Usage: clojure -M:run <path-to-chat-file>")))
