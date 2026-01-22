(ns boltz.session-zero.kick
  (:require [overtone.live :refer :all]
            [boltz.core :refer [setter]]
            [hyperfiddle.rcf :refer [tests]]))

(defonce kick-group (group :head))

;; -- Main Kick --
(defsynth main-kick [amp 0.8 decay 0.3 freq 80 out-bus 0 pan 0]
  (let [env (env-gen (perc 0.01 decay) :action FREE)
        freq-env (env-gen (perc 0.01 decay) :level-scale freq)
        src1 (sin-osc:ar (+ freq (* 0.85 freq-env)))
        src2 (sin-osc:ar (+ freq (* 1.15 freq-env)))
        sig (* (+ src1 src2) env amp)
        filtered (lpf sig 1000)]
    (out out-bus (pan2 filtered pan))))

;; -- Reverb Effect --
(defsynth rev-fx [in-bus 0 mix 0.6 room 0.8 damp 0.5]
  (let [source (in:ar in-bus 2)
        wet (free-verb source mix room damp)]
    (out 0 wet)))

(defonce !kick-rev-bus (atom nil))
(setter !kick-rev-bus
        (control-bus))

(defonce !kick-rvb (atom nil))
(setter !kick-rvb
        (rev-fx [:tail kick-group]
                :in-bus @!kick-rev-bus
                :mix 0.4
                :room 0.4
                :damp 0.2))

(comment

  (ns-unmap *ns* 'rev-fx)

  (def m
    (let [m (bus-monitor @!kick-rev-bus)]
      (add-watch m :m
                 (fn [_ _ old new]
                   (when (not= old new)
                     (println (format "Kick Monitor: %s" new)))))
      m))
  (remove-watch m :m)
  )

(tests
 "kick-rvb listen kick-rev-bus"
 (long (node-get-control @!kick-rvb :in-bus)) := (:id @!kick-rev-bus))
