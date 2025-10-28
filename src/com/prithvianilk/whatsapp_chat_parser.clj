(ns com.prithvianilk.whatsapp-chat-parser
  (:require [com.prithvianilk.whatsapp-message :refer [create-message]]
            [com.prithvianilk.parser-options :refer [create-options]])
  (:import [java.time LocalDateTime ZoneId Instant]
           [java.time.format DateTimeFormatter]
           [java.util Locale]
           [java.io BufferedReader FileReader]))

(def ^:private message-pattern
  #"^\[(\d{2}/\d{2}/\d{2}), (\d{1,2}:\d{2}:\d{2} [AP]M)\] ([^:]+):(.*)$")

(def ^:private date-time-formatter
  (DateTimeFormatter/ofPattern "dd/MM/yy, h:mm:ss a" Locale/ENGLISH))

(defn- parse-timestamp
  "Parse a timestamp from date and time strings"
  [date-str time-str]
  (let [date-time-str (str date-str ", " time-str)
        local-date-time (LocalDateTime/parse date-time-str date-time-formatter)
        zone-id (ZoneId/systemDefault)]
    (.toInstant (.atZone local-date-time zone-id))))

(defn- content-contains-media?
  "Check if content contains media markers"
  [content]
  (or (.contains content "image omitted")
      (.contains content "video omitted")))

(defn- parse-line
  "Parse a single line if it matches the message pattern"
  [line]
  (when-let [match (re-matches message-pattern line)]
    (let [[_ date-str time-str by content] match]
      {:timestamp (parse-timestamp date-str time-str)
       :by by
       :content content
       :matched true})))

(defn- read-continuation-lines
  "Read continuation lines until we hit another message or EOF"
  [lines]
  (loop [continuation []
         remaining lines]
    (if-let [line (first remaining)]
      (if (parse-line line)
        ;; Found next message, stop here
        [continuation remaining]
        ;; Continuation line
        (recur (conj continuation line) (rest remaining)))
      ;; EOF
      [continuation remaining])))

(defn- parse-message
  "Parse a complete message including continuation lines"
  [lines]
  (when-let [line (first lines)]
    (when-let [parsed (parse-line line)]
      (let [[continuation-lines remaining] (read-continuation-lines (rest lines))
            initial-content (:content parsed)
            ;; Remove leading space from initial content if present
            initial-content (if (and (not (empty? initial-content))
                                     (= \space (first initial-content)))
                              (subs initial-content 1)
                              initial-content)
            ;; Combine with continuation lines
            full-content (if (empty? continuation-lines)
                          initial-content
                          (str initial-content "\n" (clojure.string/join "\n" continuation-lines)))]
        {:message (create-message (:timestamp parsed) (:by parsed) full-content)
         :remaining remaining}))))

(defn- skip-to-first-message
  "Skip lines until we find a line that matches the message pattern"
  [lines]
  (drop-while #(nil? (parse-line %)) lines))

(defn- parse-messages-seq
  "Lazily parse messages from lines"
  [lines options]
  (lazy-seq
    (when-let [remaining-lines (seq (skip-to-first-message lines))]
      (when-let [{:keys [message remaining]} (parse-message remaining-lines)]
        (if (and (:omit-media options) (content-contains-media? (:content message)))
          ;; Skip this message if it contains media and omit-media is true
          (parse-messages-seq remaining options)
          ;; Include this message
          (cons message (parse-messages-seq remaining options)))))))

(defn get-messages
  "Parse WhatsApp messages from a file"
  ([file-name]
   (get-messages file-name (create-options)))
  ([file-name options]
   (with-open [reader (BufferedReader. (FileReader. file-name))]
     (doall (parse-messages-seq (line-seq reader) options)))))
