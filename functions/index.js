const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

//When a shop gets a new review, compute the average
exports.newShopReview = functions.firestore.document('reviews/{randomId}').onWrite((change, context) => {

    const shopID = change.after.data().shopUid; //Uid of the reviewed shop
    const reviewScore = change.after.data().reviewScore; //Score from 1 to 5 of the review

    var shopRef = admin.firestore().collection('shops').doc(shopID); //Reference to the reviewed shop document


    return admin.firestore().runTransaction(transaction => {
        return transaction.get(shopRef).then(shopDoc => {

            //New number of reviews for the shop, if the document already existed it's 
            //a change from before so the total stays the same
            var newNumReviews = change.before.exists ? shopDoc.data().numReviews : shopDoc.data().numReviews + 1;
            //If it's only an update to a review the old value must be subtracted before adding the new one
            var removeOld = change.before.exists ? change.before.data().reviewScore : 0;

            //Compute the old total and remove the previous score from it if it's an update instead of an insert
            var oldReviewTotal = (shopDoc.data().averageReviews * shopDoc.data().numReviews) - removeOld;
            //Compute the new average value for the reviews
            var newAvgReview = (oldReviewTotal + reviewScore) / newNumReviews;

            //Update the shop document
            return transaction.update(shopRef, {
                averageReviews: newAvgReview,
                numReviews: newNumReviews
            });
        });
    });
});