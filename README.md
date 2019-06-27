# Flight Service for Postman/Newman demo

This project is to show how we can use Postman and Newman for differents cases such as : synchrone, asynchrone and workflow.

## Content

Flight service is a Spring Boot Rest controller project which has these end points.
1) GET Search : return list of flight search.
2) GET Price : return detail of pricing.
3) POST BOOKING : posting booking.
4) GET BOOKING status : get booking status.

## Demo plan
This plan is susceptible to change and improvement

1) Synchrone test: call search and pricing
2) Asynchrone test: call booking and get booking result
3) Workflow: use pooling in order to get better performance

To add :
- Authentication
