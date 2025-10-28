(ns com.prithvianilk.whatsapp-chat-parser-test
  (:require [clojure.test :refer :all]
            [com.prithvianilk.whatsapp-chat-parser :refer [get-messages]]
            [com.prithvianilk.whatsapp-message :refer [create-message]]
            [com.prithvianilk.parser-options :refer [create-options]])
  (:import [java.time LocalDateTime ZoneId ZoneOffset Instant]
           [java.io File]))

(defn- assert-timestamp
  "Assert that a timestamp matches the expected date and time"
  [^Instant actual year month day hour minute second]
  (let [actual-date-time (LocalDateTime/ofInstant actual (ZoneId/systemDefault))]
    (is (= year (.getYear actual-date-time)))
    (is (= month (.getMonthValue actual-date-time)))
    (is (= day (.getDayOfMonth actual-date-time)))
    (is (= hour (.getHour actual-date-time)))
    (is (= minute (.getMinute actual-date-time)))
    (is (= second (.getSecond actual-date-time)))))

(deftest test-parse-messages
  (testing "Parse WhatsApp messages from example file"
    (let [file-path "resources/example.txt"
          messages (get-messages file-path)]
      
      ;; Verify we got 3 messages
      (is (= 3 (count messages)))
      
      ;; Verify first message
      (let [msg1 (first messages)]
        (is (= "Person 1" (:by msg1)))
        (is (= "Hi" (:content msg1)))
        (assert-timestamp (:timestamp msg1) 2025 10 15 22 1 32))
      
      ;; Verify second message
      (let [msg2 (second messages)]
        (is (= "Person 1" (:by msg2)))
        (is (= "Bye. But what's up?" (:content msg2)))
        (assert-timestamp (:timestamp msg2) 2025 10 15 22 1 42))
      
      ;; Verify third message (multi-line)
      (let [msg3 (nth messages 2)]
        (is (= "Person 2" (:by msg3)))
        (is (= "nothing\nsometimes it's something\nlol" (:content msg3)))
        (assert-timestamp (:timestamp msg3) 2025 10 15 22 2 4)))))

(deftest test-empty-file
  (testing "Parse empty WhatsApp chat file"
    (let [file-path "resources/empty.txt"
          empty-file (File. file-path)]
      ;; Create empty file
      (.mkdirs (.getParentFile empty-file))
      (.createNewFile empty-file)
      
      (try
        (let [messages (get-messages file-path)]
          (is (= 0 (count messages))))
        (finally
          ;; Cleanup
          (.delete empty-file))))))

(deftest test-omitted-data-file
  (testing "Parse WhatsApp messages with omitted media"
    (let [file-path "resources/ommited_data.txt"
          options (create-options true)
          messages (get-messages file-path options)
          ;; Timestamps parsed using system default timezone
          ;; File has "9:39:21 PM" which is 21:39:21 in 24-hour format
          expected [(create-message
                     (.toInstant (.atZone (LocalDateTime/of 2025 10 17 21 39 21) (ZoneId/systemDefault)))
                     "Prithvi Anil Kumar"
                     "Hi")
                    (create-message
                     (.toInstant (.atZone (LocalDateTime/of 2025 10 17 22 22 36) (ZoneId/systemDefault)))
                     "Prithvi Anil Kumar"
                     "Hey")]]
      (is (= expected messages)))))

(deftest test-messages-can-be-processed
  (testing "Messages can be processed without errors"
    (let [file-path "resources/example.txt"
          messages (get-messages file-path)
          count (count messages)]
      (is (= 3 count)))))
