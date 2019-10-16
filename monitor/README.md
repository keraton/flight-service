# Deploy serverless monitoring function

## Create IAM user

Roles needed are Lambda, IAM, APIGateway, CloudFormation full access

### Configure aws cli profile 

```
$ aws config --profile devoxx
```

## Deploy infrastructure

```
$ npm ci
$ SLACK_URL=YOUR_SLACK_HOOK_URL APP_PWD=YOUR_APP_PASSWORD serverless deploy --aws-profile devoxx
```

_replace `YOUR_SLACK_HOOK_URL` and `YOUR_APP_PASSWORD` accordingly._

## Redeploy function only

If necessary, you can deploy your function without redeploying your infrastructure (code updates):

```
$ SLACK_URL=YOUR_SLACK_HOOK_URL APP_PWD=YOUR_APP_PASSWORD serverless deploy function --function hello --aws-profile devox
```
_replace `YOUR_SLACK_HOOK_URL` and `YOUR_APP_PASSWORD` accordingly._