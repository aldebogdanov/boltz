(ns boltz.session-zero.bass
  (:require [overtone.live :refer :all]))

;; -- Main Bass --
(defsynth main-bass [amp 0.8 freq 55 out-bus 0 decay 0.1 pan 0 cutoff 800]
  (let [detune 0.002
        saw-env (env-gen (perc 0.01 (* 2 decay)) :action FREE)
        saw-1 (saw (+ freq (* detune freq)))
        saw-2 (saw (- freq (* detune freq)))
        sig (+ (* saw-1 saw-env (* 0.7 amp)) (* saw-2 saw-env (* 0.7 amp)))
        filtered (lpf sig cutoff)]
    (out out-bus (pan2 filtered pan))))
