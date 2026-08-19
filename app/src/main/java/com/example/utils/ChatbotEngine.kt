package com.example.utils

import com.example.data.*
import kotlinx.coroutines.flow.first

class ChatbotEngine(private val repository: AppRepository) {

    suspend fun processMessage(message: String): String {
        val lowerMsg = message.lowercase().trim()

        // 1. Add Person (Mason/Labour)
        if (lowerMsg.contains("add mason") || lowerMsg.contains("new mason") || (lowerMsg.contains("mason") && lowerMsg.contains("add"))) {
            val words = lowerMsg.split(" ")
            val nameIndex = words.indexOf("mason") + 1
            if (nameIndex > 0 && nameIndex < words.size) {
                var name = words[nameIndex]
                if (name == "hisabe" || name == "hisebe") {
                   name = words.getOrNull(words.indexOf("mason") - 1) ?: "Unknown"
                }
                repository.insertPerson(Person(name = name.replaceFirstChar { it.uppercase() }, isMason = true))
                return "Mason $name added successfully."
            }
        }
        
        if (lowerMsg.contains("add labour") || lowerMsg.contains("new labour") || lowerMsg.contains("helper") || (lowerMsg.contains("labour") && lowerMsg.contains("add"))) {
            val words = lowerMsg.split(" ")
            val keyword = if (lowerMsg.contains("helper")) "helper" else "labour"
            val nameIndex = words.indexOf(keyword) + 1
            if (nameIndex > 0 && nameIndex < words.size) {
                var name = words[nameIndex]
                if (name == "hisabe" || name == "hisebe") {
                   name = words.getOrNull(words.indexOf(keyword) - 1) ?: "Unknown"
                }
                repository.insertPerson(Person(name = name.replaceFirstChar { it.uppercase() }, isMason = false))
                return "Helper $name added successfully."
            }
        }

        // 2. Add Floor
        if (lowerMsg.contains("add floor") || (lowerMsg.contains("floor") && lowerMsg.contains("add"))) {
            val words = lowerMsg.split(" ")
            val nameMatch = Regex("(?:add floor|floor add koro) (.+)").find(lowerMsg) 
                ?: Regex("(.+) floor add koro").find(lowerMsg)
                ?: Regex("add (.+) floor").find(lowerMsg)
                
            var floorName = nameMatch?.groupValues?.get(1)?.trim()
            if (floorName.isNullOrEmpty()) {
                val idx = words.indexOf("floor")
                if (idx > 0) floorName = words[idx - 1]
            }
            if (!floorName.isNullOrEmpty() && floorName != "add") {
                floorName = floorName.replaceFirstChar { it.uppercase() } + " Floor"
                repository.insertFloor(Floor(name = floorName))
                return "Floor $floorName added successfully."
            } else {
                 repository.insertFloor(Floor(name = "New Floor"))
                 return "New Floor added."
            }
        }

        // 3. Add Flat
        if (lowerMsg.contains("add flat") || (lowerMsg.contains("flat") && lowerMsg.contains("add"))) {
            val flatNameMatch = Regex("flat ([a-z0-9]+)").find(lowerMsg)
            val flatName = flatNameMatch?.groupValues?.get(1)?.uppercase() ?: "New Flat"
            
            val floors = repository.allFloors.first()
            var targetFloorId = floors.firstOrNull()?.id ?: 0
            
            for (floor in floors) {
                if (lowerMsg.contains(floor.name.lowercase().replace(" floor", ""))) {
                    targetFloorId = floor.id
                    break
                }
            }
            
            if (targetFloorId != 0) {
                repository.insertFlat(Flat(name = flatName, floorId = targetFloorId))
                return "Flat $flatName added successfully to floor."
            } else {
                return "Cannot find a floor to add the flat. Please add a floor first or specify it."
            }
        }

        // 4. Add Work Entry
        val isWork = lowerMsg.contains("work") || lowerMsg.contains("kaj") || lowerMsg.contains("column") || lowerMsg.contains("did") || lowerMsg.contains("koreche") || lowerMsg.contains("done")
        if (isWork) {
            val persons = repository.allPersons.first()
            val workColumns = repository.allWorkColumns.first()
            
            var foundPerson: Person? = null
            for (p in persons) {
                if (lowerMsg.contains(p.name.lowercase())) {
                    foundPerson = p
                    break
                }
            }
            
            var foundColumn: WorkColumn? = null
            for (col in workColumns) {
                if (lowerMsg.contains(col.name.lowercase())) {
                    foundColumn = col
                    break
                }
            }
            if (foundColumn == null && workColumns.isNotEmpty()) {
                foundColumn = workColumns.first() // default
            }

            if (foundPerson != null && foundColumn != null) {
                val flats = repository.allFlats.first()
                var foundFlat: Flat? = null
                for (f in flats) {
                    if (lowerMsg.contains("flat " + f.name.lowercase())) {
                        foundFlat = f
                        break
                    }
                }
                
                if (foundFlat != null) {
                    repository.insertWorkEntry(WorkEntry(
                        masonId = if (foundPerson.isMason) foundPerson.id else null,
                        helperId = if (!foundPerson.isMason) foundPerson.id else null,
                        workColumnId = foundColumn.id,
                        flatId = foundFlat.id,
                        date = System.currentTimeMillis()
                    ))
                    return "Added ${foundColumn.name} for ${foundPerson.name} in Flat ${foundFlat.name}."
                } else {
                    repository.insertOtherWorkEntry(OtherWorkEntry(
                        personId = foundPerson.id,
                        helperId = null,
                        description = foundColumn.name,
                        date = System.currentTimeMillis()
                    ))
                    return "Added ${foundColumn.name} for ${foundPerson.name} as other work (no flat specified)."
                }
            } else if (foundPerson == null) {
                return "I couldn't understand who did the work. Please mention a person's name."
            }
        }

        return "I didn't understand that command. Try something like 'Add mason Azahar' or 'Azahar did column on flat 1A'."
    }
}
