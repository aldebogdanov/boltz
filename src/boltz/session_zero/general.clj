(ns boltz.session-zero.general
  (:require [overtone.live :refer :all]))

(defonce synth-group (group :head))

(defonce fx-group (group :after synth-group))

