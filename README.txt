Emma Neary and Peter Mehler

Names of files required to run program: 
SentAnalysis.java, SentAnalysisBest.java

Known Bugs:
None

Discussion 

Our classifier (before improvement) failed on the following inputs:
-I would do bad things to have this
Our classifier mis-classified this as a negative comment.  Because we are focusing only on the words, broader themes and more subtle meaning in text are lost.  The word "bad" in particular played a large role in the negative assignment, regardless of context.

-This is not a great, amazing , or even good show
For classifiers focusing only on individual words, negation is a very difficult case to handle.  There are so many positive words in this sentence, but they are negated.  The negativity of the word "not" is not well represented by this method.

-Who could love someone like him?
Here again the focus on the word love does not capture a more complex questioning of that love.  

Improved Classifier:

Using these outputs, I’d like you to evaluate how well your best system works.  In particular, I’d like you to compare your base system (that you completed in part 3) to your best classifier(that you completed in part 4). In your ReadMe, please discuss how the two systems compare based on their accuracy and precision. Also reflect on why you think the systems performed well (or poorly).  Describe some ways that you could improve the system even more. 