import argparse

class Args :
   def __init__(self, infile, outfile, configFile):
      self.parser = argparse.ArgumentParser()
      self.parser.add_argument('--i',default='occurrence_qc.json', help="Defaults to occurrence_qc.json if '--i' absent")
      self.parser.add_argument('--o',default='outcomeStats.xlsx', help="Defaults to outcomeStats.xlsx if '--o' absent")
      self.parser.add_argument('--c',default='stats.ini', help="Defaults to stats.ini if --c absent")
      self.args = self.parser.parse_args()
      self.infile = infile
      self.outfile = outfile
      self.configFile = configFile
      
   def getArgs(self):
      return self.args

   def getInfile(self):
       return self.infile

   def getOutfile(self):
       return self.outfile

   def getConfigfile(self):
       return self.configFile

def main():
   import pprint
#   print("Args.main()")
   args=Args('occurrence_qc.json', 'outcomeStats.xlsx', 'stats.ini')
   print("a=",args)
   pprint.pprint(args.getArgs())
   print("infile=",args.getInfile(), "outfile=", args.getOutfile(), "configFile=", args.getConfigfile())
#   print(args.i)



if __name__ == "__main__" :
   main()

