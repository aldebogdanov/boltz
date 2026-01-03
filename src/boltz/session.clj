(ns boltz.session
  {:clj-kondo/config {:linters {:unresolved-symbol {:level :off}}}}
  (:use overtone.live))

(definst super-saw [freq 440 amp 0.5 dur 0.5 cutoff 200]
  (let [;; 1. CREATE THE SOURCE
        ;; We create 8 saw waves with random detuning
        ;; "repeat 8" makes a list of 8 oscillators
        detuned-saws (map (fn [_i] 
                            (let [detuned (mul-add (lf-noise1:kr 0.5) 1.5 freq)]
                              (saw detuned))) 
                          (range 4))
        src (* (mix detuned-saws) (/ 1 8))
        
        ;; 2. SPREAD THEM (Stereo Width)
        ;; Splay pushes the sound to the sides, making it "wide"
        width (splay src)

        ;; 3. FILTER & ENVELOPE
        ;; Use a Low Pass Filter (LPF) to tame the harsh highs
        env (env-gen (env-perc 0.01 0.2) :action FREE)
        filt (lpf width cutoff)]
        
    ;; 4. OUTPUT
    (* filt env amp 8.0)))


(definst industrial-kick [amp 0.8 decay 1.6 freq 55]
  (let [env (env-gen (perc 0.001 decay) :action FREE)
        f-env (env-gen (perc 0.001 decay) :level-scale 80 #_freq) ;; Pitch drop
        src (sin-osc (+ freq f-env))
        dist (tanh (* 3 src))] ;; Tanh distortion is essential for "Industrial"
    (* dist env amp)))

(definst glitch-hat [amp 0.5 pan 0]
  (let [env (env-gen (perc 0.005 0.05) :action FREE)
        noise (white-noise)
        ;; Band Pass Filter with high resonance for "metallic" tone
        metal (bpf noise (t-rand 1000 8000 (impulse:kr 10)) 0.1)]
    (pan2 (* metal env amp) pan)))

(industrial-kick)
(glitch-hat)

(def m (metronome 128))
(m :bpm 190)

(defn player [beat]
  ;; Schedule the next call for 1 beat later
  (apply-at (m (inc beat)) #'player [(inc beat)])

  ;; 1. Play Kick on every beat (4/4)
  (at (m beat) (industrial-kick))

  ;; 2. Play Off-beat Hats (Techno logic: "and" check)
  (at (m beat) (glitch-hat :amp 1.0))
  (at (m (+ 0.5 beat)) (glitch-hat :amp 1.5))

  ;; 3. Probabilistic Glitch (The "IDM" factor)
  ;; 30% chance to play a random glitch in between beats
  (when (< (rand) 0.1)
    (at (m (+ 0.25 beat)) (glitch-hat :pan ( - (* (rand) 2) 1) :amp 1.0)))
)


(defn note->hz
  [n]
  (-> n note-info :midi-note midi->hz))

(def bassline [:A1 :A1 :A1 :A1])

(note->hz :A1)


(defn play-bass-at [beat]
  #_(println "Playing BASSLINE!")
  (doseq [[i note] (map-indexed vector @#'bassline)]
    (at (m (+ beat i))
      (super-saw :freq (note->hz note) :amp 8.0))
    (at (m (+ beat i (/ 1 3)))
      (super-saw :freq (note->hz note) :amp 7.0))
    (at (m (+ beat i (/ 2 3)))
        (super-saw :freq (note->hz note) :amp 6.0))))


(defn repeat-every [interval-beats func m start-beat]
  (apply-at (m start-beat) func [start-beat])
  (let [next-beat (+ start-beat interval-beats)]
    (apply-at (m next-beat)
              #'repeat-every
              [interval-beats func m next-beat])))

(repeat-every 4 #'play-bass-at #'m (#'m))

(player(#'m))

(comment

  (stop)
  (server-info)
  (boot-external-server)
  (kill-server)

  )


(config-set! :sched-ahead-time 0.3)
(config-set! :server-latency 0.15)
