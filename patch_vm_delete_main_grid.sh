cat << 'INNER_EOF' > tmp_init.txt
    init {
        viewModelScope.launch {
            repository.allWorkColumns.take(1).collect { cols ->
                if (cols.size < 26) {
                    cols.forEach { repository.deleteWorkColumn(it) }
                    val newCols = ('A'..'Z').mapIndexed { index, c -> 
                        WorkColumn(name = "", displayOrder = index, isSequential = false, requiresColumnId = null)
                    }
                    newCols.forEach { repository.insertWorkColumn(it) }
                }
            }
            
            repository.allFloors.take(1).collect { floors ->
                floors.find { it.name == "Main Grid" }?.let { mainGrid ->
                    repository.deleteFloor(mainGrid)
                }
                floors.find { it.name == "Excel Grid Floor" }?.let { excelGrid ->
                    repository.deleteFloor(excelGrid)
                }
            }
        }
    }
INNER_EOF

sed -i '/init {/,/    }/d' app/src/main/java/com/example/ui/AppViewModel.kt
sed -i '/val allMasons:/r tmp_init.txt' app/src/main/java/com/example/ui/AppViewModel.kt
