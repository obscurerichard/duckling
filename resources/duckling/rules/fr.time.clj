(
  ;; generic
  
  "intersect"
  [(dim :time #(not (:latent %))) (dim :time #(not (:latent %)))] ; sequence of two tokens with a time dimension
  (intersect %1 %2)

  ; same thing, with "de" in between like "mardi de la semaine dernière"
  "intersect by 'de' or ','"
  [(dim :time #(not (:latent %))) #"(?i)de|," (dim :time #(not (:latent %)))] ; sequence of two tokens with a time fn
  (intersect %1 %3)
  
   ;;;;;;;;;;;;;;;;;;;
  ;; Named things

  "named-day"
  #"(?i)lun\.?(di)?"
  (day-of-week 1)

  "named-day"
  #"(?i)mar\.?(di)?"
  (day-of-week 2)

  "named-day"
  #"(?i)mer\.?(credi)?"
  (day-of-week 3)

  "named-day"
  #"(?i)jeu\.?(di)?"
  (day-of-week 4)

  "named-day"
  #"(?i)ven\.?(dredi)?"
  (day-of-week 5)

  "named-day"
  #"(?i)sam\.?(edi)?"
  (day-of-week 6)

  "named-day"
  #"(?i)dim\.?(anche)?"
  (day-of-week 7)

  "named-month"
  #"(?i)janvier|janv\.?"
  (month 1)

  "named-month"
  #"(?i)fevrier|février|fev|fév\.?"
  (month 2)

  "named-month"
  #"(?i)mars|mar\.?"
  (month 3)

  "named-month"
  #"(?i)avril|avr\.?"
  (month 4)

  "named-month"
  #"(?i)mai"
  (month 5)

  "named-month"
  #"(?i)juin|jun\.?"
  (month 6)

  "named-month"
  #"(?i)juillet|juil?\."
  (month 7)

  "named-month"
  #"(?i)aout|août|aou\.?"
  (month 8)

  "named-month"
  #"(?i)septembre|sept?\.?"
  (month 9)

  "named-month"
  #"(?i)octobre|oct\.?"
  (month 10)

  "named-month"
  #"(?i)novembre|nov\.?"
  (month 11)

  "named-month"
  #"(?i)décembre|decembre|déc\.?|dec\.?"
  (month 12)

  ; Holiday TODO: check online holidays
  "noel"
  #"(?i)(jour de )?no[eë]l"
  (month-day 12 25)
  
  "jour de l'an"
  #"(?i)(jour de l'|nouvel )an"
  (month-day 1 1)

  "maintenant"
  #"maintenant|(tout de suite)"
  (cycle-nth :second 0)
  
  "aujourd'hui"
  #"(?i)(aujourd'? ?hui)|(ce jour)|(dans la journ[ée]e?)|(en ce moment)"
  (cycle-nth :day 0)

  "demain"
  #"(?i)demain"
  (cycle-nth :day 1)

  "hier"
  #"(?i)hier"
  (cycle-nth :day -1)

  "après-demain"
  #"(?i)apr(e|è)s[- ]?demain"
  (cycle-nth :day 2)

  "avant-hier"
  #"(?i)avant[- ]?hier"
  (cycle-nth :day -2)

  ;;
  ;; This, Next, Last

  "ce <day-of-week>" ; assumed to be in the future "ce dimanche"
  [#"(?i)ce" {:form :day-of-week}]
  (pred-nth-not-immediate %2 0)

  ;; for other preds, it can be immediate:
  ;; "ce mois" => now is part of it
  ; See also: cycles in en.cycles.clj
  "ce <time>"
  [#"(?i)ce" (dim :time)]
  (pred-nth %2 0)

  "<named-month|named-day> prochain|suivant"
  [(dim :time) #"(?i)prochain|suivant"]
  (pred-nth %1 1)

  "<named-month|named-day> dernier|passé"
  [(dim :time) #"(?i)derni[eéè]re?|pass[ée]e?"]
  (pred-nth %1 -1)

  "<named-day> en huit" ; would need assumption to handle 1 or 2 weeks depending on the day-of-week
  [{:form :day-of-week} #"(?i)en (huit|8)"]
  (pred-nth %1 1)

  "<named-day> en quinze" ; would need assumption to handle 2 or 3 weeks depending on the day-of-week
  [{:form :day-of-week} #"(?i)en (quinze|15)"]
  (pred-nth %1 2)

  "dernier <day-of-week> de <time>"
  [#"(?i)derni[eéè]re?" {:form :day-of-week} #"(?i)d['e]" (dim :time)]
  (pred-last-of %2 %4)
  
  "dernier <cycle> de <time>"
  [#"(?i)derni[eéè]re?" (dim :cycle) #"(?i)d['e]" (dim :time)]
  (cycle-last-of %2 %4)  
  

  ; Years
  ; Between 1000 and 2100 we assume it's a year
  ; Outside of this, it's safer to consider it's latent
  
  "year"
  (integer 1000 2100)
  (year (:value %1))

  "year (latent)"
  (integer -10000 999)
  (assoc (year (:value %1)) :latent true)

  "year (latent)"
  (integer 2101 10000)
  (assoc (year (:value %1)) :latent true)

  ; Day of month appears in the following context:
  ; - le premier
  ; - le 5
  ; - 5 March
  ; - mm/dd (and other numerical formats like yyyy-mm-dd etc.)
  ; We remove the rule with just (integer 1 31) as it was too messy

  "day of month (premier)"
  [#"(?i)premier|prem\.?|1er|1 er"]
  (day-of-month 1)

  "le <day-of-month> (non ordinal)" ; this one is latent
  [#"(?i)le" (integer 1 31)]
  (assoc (day-of-month (:value %2)) :latent true)
  
  "<day-of-month> <named-month>" ; 12 mars
  [(integer 1 31) {:form :month}]
  (intersect %2 (day-of-month (:value %1)))
  
  "<day-of-week> <day-of-month>" ; vendredi 13
  [{:form :day-of-week} (integer 1 31)]
  (intersect %1 (day-of-month (:value %2)))


  ;; hours and minutes (absolute time)
  "time-of-day (latent)"
  (integer 0 23)
  (assoc (hour (:value %1) true) :latent true)
  
  "midi"
  #"(?i)midi"
  (hour 12 false)

  "minuit"
  #"(?i)minuit"
  (hour 0 false)

  "<time-of-day> heures"
  [#(:full-hour %) #"(?i)h\.?(eure)?s?"]
  (dissoc %1 :latent) 
  
  "à|vers <time-of-day>" ; absorption
  [#"(?i)[aà]|vers" {:form :time-of-day}]
  (dissoc %2 :latent) 

  "hh(:|h)mm (time-of-day)"
  #"(?i)((?:[01]?\d)|(?:2[0-3]))[:h]([0-5]\d)"
  (hour-minute (Integer/parseInt (first (:groups %1)))
               (Integer/parseInt (second (:groups %1)))
               true)
  
  "hhmm (military time-of-day)"
  #"(?i)((?:[01]?\d)|(?:2[0-3]))([0-5]\d)"
  (-> (hour-minute (Integer/parseInt (first (:groups %1)))
                (Integer/parseInt (second (:groups %1)))
                false) ; not a 12-hour clock
      (assoc :latent true))
    
  "quart (relative minutes)"
  #"(?i)quart"
  {:relative-minutes 15}

  "trois quarts (relative minutes)"
  #"(?i)(3|trois) quarts?"
  {:relative-minutes 45}

  "demi (relative minutes)"
  #"demie?"
  {:relative-minutes 30}

  "number (as relative minutes)"
  (integer 1 59)
  {:relative-minutes (:value %1)}
  
  "number minutes (as relative minutes)"
  [(integer 1 59) #"(?i)min\.?(ute)?s?"]
  {:relative-minutes (:value %1)}

  "<hour-of-day> <integer> (as relative minutes)"
  [(dim :time :full-hour) #(:relative-minutes %)] ;before  [{:for-relative-minutes true} #(:relative-minutes %)]
  (hour-relativemin (:full-hour %1) (:relative-minutes %2) (:twelve-hour-clock? %1))

  "<hour-of-day> moins <integer> (as relative minutes)"
  [(dim :time :full-hour) #"moins( le)?" #(:relative-minutes %)]
  (hour-relativemin (:full-hour %1) (- (:relative-minutes %3)) (:twelve-hour-clock? %1))

  "<hour-of-day> et|passé de <relative minutes>"
  [(dim :time :full-hour) #"et|(pass[ée]e? de)" #(:relative-minutes %)]
  (hour-relativemin (:full-hour %1) (:relative-minutes %3) (:twelve-hour-clock? %1))
  

  ;; Formatted dates and times

  "dd/mm/yyyy"
  #"([012]?\d|30|31)/(0?\d|10|11|12)/(\d{2,4})"
  (parse-dmy (first (:groups %1)) (second (:groups %1)) (nth (:groups %1) 2) true)

  "yyyy-mm-dd"
  #"(\d{2,4})-(0?\d|10|11|12)-([012]?\d|30|31)"
  (parse-dmy (nth (:groups %1) 2) (second (:groups %1)) (first (:groups %1)) true)
  
  "dd/mm"
  #"([012]?\d|30|31)/(0?\d|10|11|12)"
  (parse-dmy (first (:groups %1)) (second (:groups %1)) nil true)
  

  ; Part of day (morning, evening...). They are intervals.

  "matin"
  #"(?i)mat(in[ée]?e?)?"
  (assoc (interval (hour 4 false) (hour 12 false) false) :form :part-of-day :latent true)

  "après-midi"
  #"(?i)apr[eéè]s?[ \-]?midi"
  (assoc (interval (hour 12 false) (hour 19 false) false) :form :part-of-day :latent true)
  
  "soir"
  #"(?i)soir[ée]?e?"
  (assoc (interval (hour 18 false) (hour 0 false) false) :form :part-of-day :latent true)
  
  "du|dans le <part-of-day>" ;; removes latent
  [#"(?i)du|dans l[ae']? ?|au|le|la" {:form :part-of-day}]
  (dissoc %2 :latent)
  
  "ce <part-of-day>"
  [#"(?i)cet?t?e?" {:form :part-of-day}]
  (assoc (intersect (cycle-nth :day 0) %2) :form :part-of-day) ;; removes :latent

  "<dim time> <part-of-day>" ; since "morning" "evening" etc. are latent, general time+time is blocked
  [(dim :time) {:form :part-of-day}]
  (intersect %1 %2)

  ;specific rule to address "3 in the morning","3h du matin" and extend morning span from 0 to 12
  "<dim time> du matin" 
  [{:form :time-of-day} #"du mat(in)?"]
  (intersect %1 (assoc (interval (hour 0 false) (hour 12 false) false) :form :part-of-day :latent true))

   "<part-of-day> du <dim time>"
   [{:form :part-of-day} #"(?i)du" (dim :time)]
   (intersect %3 %1)

  ; Other intervals: week-end, seasons
  "week-end"
  #"(?i)week(\s|-)?end"
  (interval (intersect (day-of-week 5) (hour 18 false))
            (intersect (day-of-week 1) (hour 0 false))
            false)

  "season"
  #"(?i)(cet )?été" ;could be smarter and take the exact hour into account... also some years the day can change
  (interval (month-day 6 21) (month-day 9 23) false)

  "season"
  #"(?i)(cet )?automne"
  (interval (month-day 9 23) (month-day 12 21) false)

  "season"
  #"(?i)(cet )?hiver"
  (interval (month-day 12 21) (month-day 3 20) false)

  "season"
  #"(?i)(ce )?printemps"
  (interval (month-day 3 20) (month-day 6 21) false)
  
  ; Absorptions
  
  ; a specific version of "le", above, removes :latent for integer as day of month
  ; this one is more general but does not remove latency
  "le <time>"
  [#"(?i)le" (dim :time #(not (:latent %)))]
  %2

  ; Time zones
  
  "timezone"
  #"(?i)(YEKT|YEKST|YAPT|YAKT|YAKST|WT|WST|WITA|WIT|WIB|WGT|WGST|WFT|WEZ|WET|WESZ|WEST|WAT|WAST|VUT|VLAT|VLAST|VET|UZT|UYT|UYST|UTC|ULAT|TVT|TMT|TLT|TKT|TJT|TFT|TAHT|SST|SRT|SGT|SCT|SBT|SAST|SAMT|RET|PYT|PYST|PWT|PT|PST|PONT|PMST|PMDT|PKT|PHT|PHOT|PGT|PETT|PETST|PET|PDT|OMST|OMSST|NZST|NZDT|NUT|NST|NPT|NOVT|NOVST|NFT|NDT|NCT|MYT|MVT|MUT|MST|MSK|MSD|MMT|MHT|MEZ|MESZ|MDT|MAWT|MART|MAGT|MAGST|LINT|LHST|LHDT|KUYT|KST|KRAT|KRAST|KGT|JST|IST|IRST|IRKT|IRKST|IRDT|IOT|IDT|ICT|HOVT|HNY|HNT|HNR|HNP|HNE|HNC|HNA|HLV|HKT|HAY|HAT|HAST|HAR|HAP|HAE|HADT|HAC|HAA|GYT|GST|GMT|GILT|GFT|GET|GAMT|GALT|FNT|FKT|FKST|FJT|FJST|ET|EST|EGT|EGST|EET|EEST|EDT|ECT|EAT|EAST|EASST|DAVT|ChST|CXT|CVT|CST|COT|CLT|CLST|CKT|CHAST|CHADT|CET|CEST|CDT|CCT|CAT|CAST|BTT|BST|BRT|BRST|BOT|BNT|AZT|AZST|AZOT|AZOST|AWST|AWDT|AST|ART|AQTT|ANAT|ANAST|AMT|AMST|ALMT|AKST|AKDT|AFT|AEST|AEDT|ADT|ACST|ACDT)"
  {:dim :timezone
   :value (-> %1 :groups first .toUpperCase)}
  
  "<time> timezone"
  [(dim :time) (dim :timezone)]
  (set-timezone %1 (:value %2))

  ; Intervals

  "dd-dd <month>(interval)"
  [#"([012]?\d|30|31)" #"\-|au|jusqu'au" #"([012]?\d|30|31)" {:form :month}]
  (interval (intersect %4 (day-of-month (Integer/parseInt (-> %1 :groups first))))
            (intersect %4 (day-of-month (Integer/parseInt (-> %3 :groups first))))
            true)

  "entre dd et dd <month>(interval)"
  [#"entre( le)?" #"([012]?\d|30|31)" #"et( le)?" #"([012]?\d|30|31)" {:form :month}]
  (interval (intersect %5 (day-of-month (Integer/parseInt (-> %2 :groups first))))
            (intersect %5 (day-of-month (Integer/parseInt (-> %4 :groups first))))
            true)

  ; Blocked for :latent time. May need to accept certain latents only, like hours

  "<datetime> - <datetime> (interval)"
  [(dim :time #(not (:latent %))) #"\-|au|jusqu'(au|à)" (dim :time #(not (:latent %)))]
  (interval %1 %3 true)

  "de <datetime> - <datetime> (interval)"
  [#"(?i)de|depuis" (dim :time) #"\-|au|jusqu'(au|à)" (dim :time)]
  (interval %2 %4 true)

  "entre <datetime> et <datetime> (interval)"
  [#"(?i)entre" (dim :time) #"et" (dim :time)]
  (interval %2 %4 true)

  ; Specific for time-of-day, to help resolve ambiguities

  "<time-of-day> - <time-of-day> (interval)"
  [{:form :time-of-day} #"\-|à|au|jusqu'(au|à)" {:form :time-of-day}]
  (interval %1 %3 true)

  "de <time-of-day> - <time-of-day> (interval)"
  [#"(?i)de" {:form :time-of-day} #"\-|à|au|jusqu'(au|à)" {:form :time-of-day}]
  (interval %2 %4 true)

  "entre <time-of-day> et <time-of-day> (interval)"
  [#"(?i)entre" {:form :time-of-day} #"et" {:form :time-of-day}]
  (interval %2 %4 true)

  "avant <time-of-day>(interval)"
  [#"(?i)avant" {:form :time-of-day}]
  (interval (cycle-nth :second 0) %2 false)

  ; Specific for within duration... Would need to be reworked to adapt the grain
  "d'ici <duration>"
  [#"(?i)d'ici" (dim :duration)]
  (interval (cycle-nth :second 0) (in-duration (:value %2)) false)

)
