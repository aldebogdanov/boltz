(ns boltz.session-zero
  (:require [overtone.live :refer :all]
            [hyperfiddle.rcf]
            [boltz.core :refer [metro start-player stop-player restart-player
                                set-bpm-and-restart pset! pdrop! note->hz]]
            [boltz.session-zero.kick :refer [main-kick kick-group !kick-rev-bus]]
            [boltz.session-zero.bass :refer [main-bass]]
            [boltz.session-zero.lead :refer [lead lead-group !lead-cutoff-bus]]
            [boltz.session-zero.crash :refer [soft-massive-crash]]
            [overtone.inst.drum :as d]
            [overtone.inst.synth :as s]))

(defn kick-4-4 [beat]
  (when (zero? (mod beat 4))
    (doseq [i (range 4)]
      (at (metro (+ beat i)) (main-kick [:head kick-group] :out-bus @!kick-rev-bus :freq 55 :amp 0.5)))))

(defn dub-kick [beat]
  (at (metro beat) (d/dub-kick)))

(defn bass-triple [beat]
  (doseq [i (range 3)]
    (let [b (+ beat (/ i 3))]
      (at (metro b) (main-bass :freq 55 :out-bus 0 :cutoff 300 :decay 0.06)))))

(defn hats-8 [beat]
  (doseq [i (range 2)]
    (let [b (+ beat (/ i 2))
          a (if (zero? i) 0.05 0.01)]
      (at (metro b) (d/hat3 :amp a :t 0.02)))))

(defn toms [b]
  (when (zero? (mod b 4))
    (at (metro (+ b 3.75))(d/tom 110 0.4))
    (at (metro (+ b 0.5))(d/tom 110 0.6))
    (at (metro (+ b 1.5))(d/tom 110 0.6))))

(defn crash [b]
  (when (zero? (mod b 16))
    (at (metro b) (soft-massive-crash :release 4.0 :room 0.999))))

(defn lead-atonal [beat]
  (when (zero? (mod beat 4))
    (let [tone3   (/ 42 32)
          aug-4th (/ 729 512)
          n :A2
          f (note->hz n)
          lead-1 (at (metro beat) (lead [:head lead-group] :freq f :amp 0.1 :cutoff-bus @!lead-cutoff-bus))
          lead-2 (at (metro beat) (lead [:head lead-group] :freq (* aug-4th f) :amp 0.1 :cutoff-bus @!lead-cutoff-bus))]
      (at (metro (+ beat 3)) (ctl lead-1 :gate 0))
      (at (metro (+ beat 2)) (ctl lead-2 :freq (* tone3 f)))
      (at (metro (+ beat 3)) (ctl lead-2 :gate 0)))))


(comment

  (ns-unmap *ns* 'lead)
  
  (s/supersaw 110)
  (stop)

  @boltz.core/!patterns
  
  (pset! :l 'lead-atonal)
  (pdrop! :l)

  (pset! :t 'toms)
  (pdrop! :t)
  (pset! :c 'crash)
  
  (pset! :k 'kick-4-4
         :b 'bass-triple
         :h 'hats-8)
  (pdrop! :h)
  (pdrop! :k)

  (pdrop! :k :h :b)

  (d/snare)
  (main-kick :freq 55)
  (main-bass)
  
  ;; ===== CONTROL =====
  (start-player)   ; Start with current code
  (stop-player)    ; Stop completely
  (restart-player) ; Stop and start with latest code
  
  ;; ===== TEMPO =====
  (set-bpm-and-restart 128)  ; Change BPM, restart if playing
  (set-bpm-and-restart 190)
  (set-bpm-and-restart 0)
  @!bpm  ; Check current BPM
  
  ;; ===== INSTRUMENTS =====
  (toggle-kick)
  (toggle-bass)
  (toggle-hats)
  
  ;; See current states:
  {:kick @!kick? :bass @!bass? :hats @!hats?}
  
  ;; ===== DEBUG =====
  (server-info)
  (node-tree)
  
  ;; ===== EXPERIMENT =====
  ;; Modify the player function above, then:
  (restart-player)  ; Instantly hear changes
  
  ;; Try new patterns in isolation:
  (do
    (toggle-hats)
    (at (m (inc (m))) #(d/hat3 :amp 0.4)))
  
  ;; ===== CLEANUP =====
  (stop-player)
  (kill-server)
  (boot-server)
  (stop)
)
