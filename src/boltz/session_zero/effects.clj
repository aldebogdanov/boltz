(ns boltz.session-zero.effects
  (:require [overtone.live :refer :all]
            [boltz.common :refer [defnode]]
            [boltz.session-zero.general :refer [fx-group]]
            [hyperfiddle.rcf :refer [tests]]))

;; -- Main Kick Reverb Effect --
(defsynth rev-fx [in-bus 0 mix 0.6 room 0.8 damp 0.5]
  (let [source (in:ar in-bus 2)
        wet (free-verb source mix room damp)]
    (out 0 wet)))


(defonce kick-rev-bus (audio-bus 2))

(defnode kick-rvb
  (rev-fx [:tail fx-group]
          :in-bus kick-rev-bus
          :mix 0.12
          :room 0.7
          :damp 0.1))

;; -- Main Bass Declip Effect --
(defsynth declip-fx [in-bus 0 thresh 0.8]
  (let [source (in:ar in-bus 2)
        clipped (clip2 source thresh)]
    (out 0 clipped)))


(tests
 "kick-rvb listen kick-rev-bus"
 (long (node-get-control kick-rvb :in-bus)) := (:id kick-rev-bus)

 (println))
