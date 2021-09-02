import pandas as pd
import os
import re


def load_csv(directory='./output'):
    for root, _, files in os.walk(directory):
        for file in files:
            epoch = file[6:7]
            filename = file[8:-4]
            if file[-4:] == '.csv':
                data = pd.read_csv(directory + "/" + file)
                print(epoch, filename)
            elif file[-4:] == '.txt':
                print(epoch, filename)



def main():
    load_csv(directory='./output')
    print("Loading results...")


if __name__ == "__main__":
    # for i in range (1,3):
    #     os.system("java -jar ./SketchingTweet-v1.jar -iu 1000 -qu 1000 -p 0 -s 0.6 -n " + str(i))
    main()