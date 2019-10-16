'use strict';

const newman = require('newman');
const path = require('path');
const { IncomingWebhook } = require('@slack/webhook');
const markdowntable = require('markdown-table');
const prettyms = require('pretty-ms');

module.exports.hello = (event, context, callback) => {
  const globals = {
    "id": "edae90c1-c3e5-468d-802e-fb34b9b0d750",
      "name": "service-auth",
      "values": [
        {
          "key": "password",
          "value": process.env.APP_PWD,
          "enabled": true
        }
      ]
  };

  const options = {
    collection: path.join(__dirname, 'collections/flight-service-monitor.postman_collection.json'),
    environment: path.join(__dirname, 'environments/gcp.postman_environment.json'),
    globals: globals,
    reporters: 'cli'
  };

  newman.run(options).on('start', () => {
    console.log('newman run started');
  }).on('done', (error, summary) => {
    if (error || summary.error) {
      console.error('Newman test run encountered an error.');
      const response_error = {
        statusCode: 400,
        body: JSON.stringify({
          message: 'Newman test run encountered an error.'
        })
      };
      callback(response_error, null);
    } else {
      const url = process.env.SLACK_URL;
      const webhook = new IncomingWebhook(url);
      const slack_message = buildSlackTable(summary);
      console.log(slack_message);
      webhook.send({
        text: slack_message,
        username: "Paul",
        icon_emoji: ":newman:"
      });

      const response_success = {
        statusCode: 200,
        body: JSON.stringify({
          message: `Newman run done. ${summary.run.stats.tests.total} tests run with ${summary.run.failures.length} failures`
        })
      };
      if (summary.run.failures.length > 0) {
        callback(null, response_success);
      } else {
        callback(null, response_success);
      }
    }
  })
};

function buildSlackTable(summary) {
  const backticks = '```';
  const run = summary.run;
  let data = [];
  let header = summary.environment.values.find(o => o.key === 'commit_id').value;
  let title = summary.collection.name;
  if (summary.environment.name) {
    title += ' - ' + summary.environment.name
  }
  const headers = [header, 'total', 'failed'];
  const arr = ['iterations', 'requests', 'testScripts', 'prerequestScripts', 'assertions'];

  data.push(headers);
  arr.forEach(function (element) {
      data.push([element, run.stats[element].total, run.stats[element].failed]);
  });

  const duration = prettyms(run.timings.completed - run.timings.started);
  data.push(['------------------', '-----', '-------']);
  data.push(['total run duration', duration]);

  const table = markdowntable(data);
  return `${title}\n${backticks}${table}${backticks}`;
}