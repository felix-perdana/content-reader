Rebuild with skipping the test
./gradlew build -x test

Any problem persists
./gradlew --stop
./gradlew clean
./gradlew build

Build the docker image
docker build --platform=linux/amd64 -t asia-northeast1-docker.pkg.dev/content-reader-250620/content-reader-backend-repo/content-reader-backend .

Login to gcloud
gcloud auth login

Push to cloud run
docker push asia-northeast1-docker.pkg.dev/content-reader-250620/content-reader-backend-repo/content-reader-backend:latest

gcloud run deploy content-reader-backend \
  --image asia-northeast1-docker.pkg.dev/content-reader-250620/content-reader-backend-repo/content-reader-backend:latest \
  --region asia-northeast1 \
  --platform managed \
  --allow-unauthenticated

