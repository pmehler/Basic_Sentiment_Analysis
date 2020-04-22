Emma Neary and Peter Mehler

Names of files required to run program:
SentAnalysis.java, SentAnalysisBest.java

Known Bugs:
None

Discussion:

The basic classifier seems to fail on inputs which include negations of adjectives and complicated ways of phrasing emotions.

For example, the base classifier failed on the following inputs:
-"I would do bad things to have this"
Our classifier misclassifies this as a negative comment. Because we are focusing only on the words, broader themes and more subtle meaning in text are lost.  The word "bad" in particular played a large role in the negative assignment, regardless of context.

-"This is not a great, amazing, or even good show"
For classifiers focusing only on individual words, negation is a very difficult case to handle. There are so many positive words in this sentence, but they are negated. The negativity of the word "not" is not factored into this method.

-"Who could love someone like him?"
Here again the focus on the word love does not capture a more complex questioning of that love. Questions can greatly change the meaning of words.

Classifier Evaluations:

To improve our base classifier, we added three features to the log probability sum: character count, number of capital letters, and number of exclamation points in a review. All three features individually increased the accuracy of our classifier:

    The accuracy of the base classifier is 78.62%
    The accuracy of the best classifier is 80.72%

The base classifier performed fairly well for just considering single word occurrences in each review. It likely misclassified 21% of reviews because sentiment can often change depending on the way words are put together. There were also many words in the reviews which were not found in the training data due to exaggeration with repeating letters or misspellings.

The accuracy of our best classifier increased by just over 2%. The increase is likely small because this is only three features in addition to tens of words in each review. However, the length, exclamation mark occurrence, and capital letter occurrence do have some relationship to sentiment because the overall accuracy did consistently increase. This is likely because these are ways that many people express tone when writing online.

The precision changes were not as clear. For the base classifier, there was a bias toward classifying documents as positive, so the positive precision suffered:

    The positive precision of the base classifier is 73.79%
    The negative precision of the base classifier is 86.95%

In the best classifier, the precision rates are somewhat more even, which is likely related to the increase in accuracy.

    The positive precision of the best classifier is 78.66%
    The negative precision of the best classifier is 83.38%

Some ways the system could further be improved is by modifying the features being considered. For example, it could consider pairs of words as features instead of single words. It could also consider more features such as other types of punctuation, word repetition, and word counts. It could also be improved by increasing the size of the training data so it has more accurate and complete reference information.
