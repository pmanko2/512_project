package TransactionManager;

import java.io.Serializable;

public enum OP_CODE implements Serializable
{
	ADD_FLIGHT,
	ADD_CARS,
	ADD_ROOMS,
	DELETE_FLIGHT,
	DELETE_CARS,
	DELETE_ROOMS,
	DELETE_CUSTOMER,
	GET_CUSTOMER_RESERVATION,
	QUERY_CUSTOMER_INFO,
	QUERY_CARS,
	QUERY_CAR_PRICE,
	QUERY_FLIGHTS,
	QUERY_FLIGHT_PRICE,
	QUERY_ROOMS,
	QUERY_ROOM_PRICE,
	NEW_CUSTOMER,
	NEW_CUSTOMER_ID,
	ITINERARY,
	RESERVE_FLIGHT,
	RESERVE_CAR,
	RESERVE_ROOM
}