# Goal
- high convience, easy to install, one click does all
- pirate scout drone spawns randomly in loaded asteroid belts with active jammer, calls for pirate raid if not destroyed fast enough

Technicality:
- event system
    - starloader eventhandlers generate info code
    - fire all custom events that match code
    - binary code that contains event info
        - 0-7: faction type
        - 8-15: entity type
        ... tbd
        - 49-56: cooldown in minutes 1..256 (4.2h)
        - 57-64: probability x/256
    - timeout
    
- spawning
    - drone
    - pirate fleet
- automated blueprint download

- AI
    - custom fleet behaviour
        - drone: wait for contact, follow contact at distance
            - drone can only see what its scanners can see
        - pirates:
            - send message requesting ship to surrender (unfaction, unfleet, leave)
            - then capture ship
            - else attack and destroy
            - if enemy is to strong (outgunned), run away
    - custom ship behaviour
        - move close to unfactioned ship to capture
        - cease fire when ship surrenders
        - stop fighting and run away if fleet is told to do so
        
- config
    - ingame way to edit the ship BPs used for the pirate fleet