(ns boltz.common
  (:require [overtone.live :refer :all]))


#_{:clj-kondo/config {:lint-as {defnode clojure.core/def}}}
(defmacro defnode
  [name body]
  `(def ~name (do #_(when (~'node-active? ~name)
                    (~'kill ~name)
                    (println (format "Node '%s' killed!" '~name)))
                  ~body)))


(defmacro monitor-bus
  [name bus]
  `(def ~(symbol name) (let [switch# (atom false)
                             monitor# (~'bus-monitor ~bus)
                             key# (keyword (gensym "monitor-bus-"))]
                         (fn []
                           (if (swap! switch# not)x3
                             (add-watch ~bus key# (fn [_# _# old# new#]
                                                    (when (not= old# new#)
                                                      (println (format "Bus monitor '%s': %s" ~name new#)))))
                             (remove-watch ~bus key#))))))


(defn note->hz
  [n]
  (-> n note-info :midi-note midi->hz))


(defn repeat-every [interval-beats func m start-beat]
  (apply-at (m start-beat) func [start-beat])
  (let [next-beat (+ start-beat interval-beats)]
    (apply-at (m next-beat)
              #'repeat-every
              [interval-beats func m next-beat])))
