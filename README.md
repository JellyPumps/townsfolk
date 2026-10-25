# Townsfolk

Townsfolk is a Fabric mod for Minecraft 1.21.1 that adds citizens to your world. Each one has a home, a job, and a daily schedule.

I'm building it for my own multi-city world, after not finding any NPC mod that would give me the features I wanted. It isn't a settlement or colony mod. Citizens never place or break blocks.

This is a hobby project, so progress may not be consistent as other things take priority.

## Features (Implemented & Planned)

- Simulate working towns and cities that feel alive
- Citizens wander in their free time like real people: some stay in, some
  exercise outside, shop for groceries, or hang out with friends and family
- Family mechanic and multiple age groups
- Schools and third places (cafes, restaurants, pubs, parks) for socialising
- Social circles and gossip
- Shopkeepers who trade with players and with other NPCs
- NPCs use Minecraft Transit Railway (MTR) for public transport
- NPCs wait for roads to be clear before crossing
- Personalities that shape how a citizen spends the day
- A config item and custom GUI to edit a citizen (name, personality,
  schedule, and later age, household and relationships)
- Possible later: my own vehicles mod, with NPC truckers and farmers

## Roadmap ( Not Ordered )

- [X] Homes, workplaces and a place registry
- [X] Time-of-day schedules
- [X] Free time and wandering
- [X] Custom A* Pathfinding
- [ ] Skin support
- [ ] Ease of use through GUI instead of commands
- [ ] Age groups
- [ ] Families
- [ ] Schools
- [ ] Third places
- [ ] Social circles
- [ ] Gossip
- [ ] Waypoint navigation
- [ ] Near and far simulation
- [ ] Crossings
- [ ] Shops
- [ ] MTR support
- [ ] Vehicle support (probably a separate mod)

## Requirements

- Minecraft 1.21.1
- Fabric Loader
- Fabric API


## Design notes

This project uses Yarn mappings.

Only citizens near a player will exist as entities, to avoid lag and unnecessary simulations. The rest are stored as data and advanced by their schedules, then spawned in the right place when a player gets close. I haven't built this yet, so the details may change.

## Contributing

Issues are open. If you report a bug, include your mod list and, for stuck citizens, the coordinates and time of day. I'm not taking pull requests yet.