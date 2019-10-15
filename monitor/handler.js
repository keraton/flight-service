'use strict';

const newman = require('newman');
const path = require('path');
const { IncomingWebhook } = require('@slack/webhook');
const markdowntable = require('markdown-table');
const prettyms = require('pretty-ms');

module.exports.hello = (event, context, callback) => {
  const options = {
    collection: path.join(__dirname, 'collections/flight-service-monitor.postman_collection.json'),
    environment: path.join(__dirname, 'environments/gcp.postman_environment.json'),
    reporters: 'cli'
  }

  // const url = 'https://hooks.slack.com/services/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';
  const url = event['queryStringParameters']['slackUrl'];
  const webhook = new IncomingWebhook(url);

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
      }
      callback(response_error, null);
    } else {
      const slack_message = buildSlackTable(summary);
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
      }
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
  let data = []
  let title;
  let header = '';
  if (!title) {
      title = summary.collection.name;
      if (summary.environment.name) {
          title += ' - ' + summary.environment.name
      }
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