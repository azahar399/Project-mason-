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
            repository.allFlats.take(1).collect { flats ->
                if (flats.size < 150) {
                    flats.forEach { repository.deleteFlat(it) }
                    val floorId = repository.insertFloor(Floor(name = "Main Grid"))
                    for (i in 1..150) {
                        repository.insertFlat(Flat(name = "$i", floorId = floorId.toInt(), sqFt = 1000.0))
                    }
                }
            }
        }
    }
INNER_EOF

sed -i '/init {/,/    }/d' app/src/main/java/com/example/ui/AppViewModel.kt
sed -i '/val allFlats:/r tmp_init.txt' app/src/main/java/com/example/ui/AppViewModel.kt
