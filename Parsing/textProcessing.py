class Question:
    def __init__(self, question, answer, category=None, valid=True):
        self.question=question
        self.answer=answer;
        self.category=category
        self.valid=valid
CATEGORY_DICT={
    'ap':"anatomy and physiology",
    "cb":"cell biology",
    "eco":"ecology",
    "et":"ethology",
    "ph":"phylogeny",
    "bc":"biochemistry",
    "misc":'misc'
}
# answer file line description:
# {answer} {x if invalid} {category, separate with /}
def getQuestionNumber(q,delim='.'):
    #assuming the question starts with number
    num=''
    for c in q:
        if c==delim:
            return int(num)
        num+=c
    return -1
def seeText(fname):
    with open(fname,'r') as fr:
        for line in fr:
            print(f'question number {getQuestionNumber(line)}: {line}')
            input()
def getQuestions(fname):
    with open(fname,'r') as fr:
        questions={getQuestionNumber(line):line.replace('{}','\n').strip() for line in fr}
    print(len(questions))
    return questions

def getAnswers(fname):
    with open(fname,'r') as fr:
        return {i+1:j for i,j in enumerate(fr)}

def makeQuizletCards(questions,answers):
    results=[]#list of 2-tuples
    for num in questions:
        front=""
        back=""
        question=questions[num]
        answerInfo=answers[num]
        if 'x' in answerInfo:
            front+="[WARNING: INVALID]\n"
        front+=question
        splitted=answerInfo.split(" ")
        back+=f'answer: {splitted[0]}\n'
        if (len(splitted)==2 and 'x' not in answerInfo) or len(splitted)==2:
            back+=f'type: {splitted[-1]}'
        front=front.strip()
        back=back.strip()
        results.append((front,back))
    return results
def cards2tsv(cardResults,fname):
    with open(fname,'w') as fw:
        for card in cardResults:
            front,back = card
            fw.write(front+'\t'+back+'\n')
def readTsv(fname):
    with open(fname,'r') as fr:
        for line in fr:
            print(line)
            front,back=line.split("\t")
            print("FRONT: ")
            print(front)
            print("BACK: ")
            print(back)
            print('#'*15)
            input("> ")

cards=makeQuizletCards(getQuestions("parsed/text17semis.txt"),getAnswers("parsed/testAnswerBank.txt"))
#print(cards[:2])
cards2tsv(cards,"parsed/quizlet17_1.tsv")
readTsv("parsed/quizlet17_1.tsv")
