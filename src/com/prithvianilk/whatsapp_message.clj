(ns com.prithvianilk.whatsapp-message)

(defrecord WhatsAppMessage [timestamp by content])

(defn create-message
  "Create a new WhatsApp message"
  [timestamp by content]
  (->WhatsAppMessage timestamp by content))
