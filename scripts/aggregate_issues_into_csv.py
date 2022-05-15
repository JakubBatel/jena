import sys
import json
import csv


with open('../table.csv', 'w') as csvFile:
    csvWriter = csv.writer(csvFile)
    csvWriter.writerow(['year', 'project', 'issue_code'])
    for file in sys.argv[1:]:
        print(file)
        filename = file.split('.')[0]
        year, project = filename.split('-')
        with open(file, 'r') as jsonFile:
            jsonData = json.load(jsonFile)
            if 'issues' not in jsonData:
                continue
            for issue in jsonData['issues']:
                issue_code = issue['name'].split(' - ')[0]
                csvWriter.writerow([year, project, issue_code])