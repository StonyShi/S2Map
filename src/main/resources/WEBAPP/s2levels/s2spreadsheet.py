#!/usr/bin/python

import csv
import sys

for l in csv.reader(open(sys.argv[1])):
  # 0 = level
  # 2 = url
  # print "wget -O %s.png \"%s\"" % (l[0], l[2])
  print "<tr><td>%s</td><td>%s</td><td><img src=\"%s.png\"></td></tr>" % (l[0], l[1], l[0])

