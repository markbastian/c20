(ns c20.spec
  (:require [clojure.spec.alpha :as s]
            [c20.core :as core]))

;;https://www.dnd5eapi.co/docs/#common-models-section

(s/def ::index string?)
(s/def ::name string?)
(s/def ::class string?)
(s/def ::full_name string?)
(s/def ::url (s/and string? #(re-matches #"(?:/[^/]+)*" %)))

(s/def ::desc (s/coll-of string?))

(s/def ::named-resource (s/keys :req-un [::name ::class ::url]))
(s/def ::api-reference (s/keys :req-un [::index ::name ::url]))
(s/def ::class-api-resource (s/keys :req-un [::index ::class ::url]))

(s/def ::choose pos-int?)
(s/def ::type string?)
(s/def ::from (s/coll-of ::api-reference))
(s/def ::choice (s/keys :req-un [::choose ::type ::from]))
(s/def ::cost (s/keys :req-un [::quantity ::unit]))

(s/def ::skills (s/coll-of ::api-reference))

(s/def ::srd-ability-score (s/and ::api-reference (s/keys :req-un [::desc ::skills ::full_name])))
(s/def ::srd-ability-scores (s/coll-of ::srd-ability-score))

(s/def ::class_levels ::url)
(s/def ::hit_die pos-int?)
(s/def ::proficiencies (s/coll-of ::api-reference))
(s/def ::proficiency_choices (s/coll-of ::choice))
(s/def ::saving_throws (s/coll-of ::api-reference))
(s/def ::spellcasting ::url)
(s/def ::spells ::url)
(s/def ::starting_equipment ::url)
(s/def ::subclasses (s/coll-of ::api-reference))

(s/def ::srd-non-spell-casting-class (s/and ::api-reference
                                            (s/keys :req-un [::class_levels
                                                             ::hit_die
                                                             ::proficiencies
                                                             ::proficiency_choices
                                                             ::saving_throws
                                                             ::starting_equipment
                                                             ::subclasses])))

(s/def ::srd-spell-casting-class (s/and ::srd-non-spell-casting-class
                                        (s/keys :req-un [::spells ::spellcasting])))

(s/def ::srd-class (s/or :srd-non-spell-casting-class ::srd-non-spell-casting-class
                         :srd-spell-casting-class ::srd-spell-casting-class))

(s/def ::srd-classes (s/coll-of ::srd-class))

;(->> "resources/5e-SRD-Classes.json" core/load-data (map :spellcasting) (take 10))
;(->> "resources/5e-SRD-Classes.json" core/load-data (take 4))
;(->> "resources/5e-SRD-Classes.json" core/load-data (s/explain ::srd-classes))

(comment
  (s/valid?
    ::api-reference
    {:desc      ["Strength measures bodily power, athletic training, and the extent to which you can exert raw physical force."
                 "A Strength check can model any attempt to lift, push, pull, or break something, to force your body through a space, or to otherwise apply brute force to a situation. The Athletics skill reflects aptitude in certain kinds of Strength checks."],
     :index     "str",
     :name      "STR",
     :skills    [{:index "athletics", :name "Athletics", :url "/api/skills/athletics"}],
     :url       "/api/ability-scores/str",
     :full_name "Strength"})

  (->> "resources/5e-SRD-Ability-Scores.json" core/load-data (s/valid? ::srd-ability-scores))
  (->> "resources/5e-SRD-Classes.json" core/load-data (s/valid? ::srd-classes))
  )
