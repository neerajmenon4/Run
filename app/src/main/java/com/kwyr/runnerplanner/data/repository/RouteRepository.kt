package com.kwyr.runnerplanner.data.repository

import com.kwyr.runnerplanner.data.local.PreferencesDataStore
import com.kwyr.runnerplanner.data.model.Route
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RouteRepository @Inject constructor(
    private val dataStore: PreferencesDataStore
) {
    val routesFlow: Flow<List<Route>> = dataStore.routesFlow

    suspend fun getAllRoutes(): Result<List<Route>> {
        return try {
            val routes = dataStore.routesFlow.first()
            Result.success(routes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveRoute(route: Route): Result<Unit> {
        return try {
            val currentRoutes = dataStore.routesFlow.first().toMutableList()
            currentRoutes.add(route)
            dataStore.saveRoutes(currentRoutes)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateRoute(updatedRoute: Route): Result<Unit> {
        return try {
            val currentRoutes = dataStore.routesFlow.first().toMutableList()
            val index = currentRoutes.indexOfFirst { it.id == updatedRoute.id }
            if (index != -1) {
                currentRoutes[index] = updatedRoute
                dataStore.saveRoutes(currentRoutes)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Route not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteRoute(routeId: String): Result<Unit> {
        return try {
            val currentRoutes = dataStore.routesFlow.first().toMutableList()
            currentRoutes.removeAll { it.id == routeId }
            dataStore.saveRoutes(currentRoutes)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRouteById(routeId: String): Result<Route?> {
        return try {
            val routes = dataStore.routesFlow.first()
            val route = routes.find { it.id == routeId }
            Result.success(route)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
