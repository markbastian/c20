(ns c20.validation
  (:require [c20.spec :as spec]
            [c20.core :as core]
            [clojure.spec.alpha :as s]))

(->> "resources/5e-SRD-Ability-Scores.json"
     core/load-data
     (s/valid? ::srd-ability-scores))
