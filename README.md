# WaesTechAssessment

Add environment variable **"env=test"** to run integration tests.  

In order to run the application you have to set the environment variable as **"env=prod"** you also need docker and to run 
**docker-compose up** and you'll be good to go.
Application is running against a containerized elasticsearch.

Once the application is running, you should be able to start posting data to it and asking for a diff calculation only
when both sides of the element have been posted. Otherwise you'll receive an error.

A couple of examples:

This will post base64 encoded data to the application for its storage. Please remember that there is another endpoint 
for the right part of the element whi actually ends up with right instead of left.

curl --request POST \
  --url http://localhost:8080/v1/diff/testId/left \
  --header 'content-type: application/json' \
  --data '{
	"data": "ewoJInRlc3Rfa2V5XzEiOiAidGVzdF92YWx1ZV8xIiwKCSJ0ZXN0X2tleV8yIjogInRlc3RfdmFsdWVfMiIsCgkidGVzdF9rZXlfMyI6ICJ0ZXN0X3ZhbHVlXzMiCn0="
}'

This will retrieve diff calculation result of both parts of element with given id.

curl --request GET \
  --url http://localhost:8080/v1/diff/testId/ 