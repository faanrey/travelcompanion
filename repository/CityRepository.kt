fun observeCities(): Flow<List<City>>
fun observeCity(cityId: String): Flow<City?> 
fun observePointsOfInterest(cityId: String): Flow<List<PointOfInterest>>
fun observePointsOfInterestByCategory(
        cityId: String,
        category: PoiCategory
    ): Flow<List<PointOfInterest>>
fun searchPointsOfInterest(query: String): Flow<List<PointOfInterest>>
suspend fun refresh(): Result<Unit> 
