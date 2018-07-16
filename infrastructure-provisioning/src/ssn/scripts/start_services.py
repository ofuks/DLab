#!/usr/bin/python
from fabric.api import *

supervisor_started = False

try:
    local('sudo systemctl is-active supervisord')
except:
    local('sudo systemctl start supervisord')
    supervisor_started = True
if not supervisor_started:
    services = list()
    list_not_running_services = local('sudo supervisorctl status all | tr -s " "', capture=True)
    for service in list_not_running_services.split('\n'):
        services.append({'name': service.split(' ')[0], 'status': service.split(' ')[1]})

    for service in services:
        if service['status'] != 'RUNNING':
            local('sudo supervisorctl restart {}'.format(service['name']))

