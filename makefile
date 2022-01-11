build:
	sam build

deploy: build
	sam build && sam deploy

deploy-guided: build
	sam deploy --guided
