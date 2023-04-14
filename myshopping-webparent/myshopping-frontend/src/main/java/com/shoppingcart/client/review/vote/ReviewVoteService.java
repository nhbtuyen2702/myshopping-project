package com.shoppingcart.client.review.vote;

import java.util.List;
import java.util.NoSuchElementException;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.shoppingcart.common.entity.Customer;
import com.shoppingcart.common.entity.Review;
import com.shoppingcart.common.entity.ReviewVote;
import com.shoppingcart.client.review.ReviewRepository;
import com.shoppingcart.client.vote.VoteResult;
import com.shoppingcart.client.vote.VoteType;

@Service
@Transactional
public class ReviewVoteService {
	
	@Autowired private ReviewRepository reviewRepo;
	@Autowired private ReviewVoteRepository voteRepo;
	
	public VoteResult undoVote(ReviewVote vote, Integer reviewId, VoteType voteType) {
		voteRepo.delete(vote);//đã vote up và nhấn vote up hoặc đã vote down và nhấn vote down -->xóa vote up hoặc vote down này đi -->có nghĩa là ko vote
		reviewRepo.updateVoteCount(reviewId);//cập nhật lại tổng số vote cho review này
		Integer voteCount = reviewRepo.getVoteCount(reviewId);
		
		return VoteResult.success("You have unvoted " + voteType + " that review.", voteCount);
	}
	
	public VoteResult doVote(Integer reviewId, Customer customer, VoteType voteType) {
		Review review = null;
		
		try {
			review = reviewRepo.findById(reviewId).get();
		} catch (NoSuchElementException ex) {
			return VoteResult.fail("The review ID " + reviewId + " no longer exists.");
		}
		
		ReviewVote vote = voteRepo.findByReviewAndCustomer(reviewId, customer.getId());
		
		if (vote != null) {
			if (vote.isUpvoted() && voteType.equals(VoteType.UP) || //nếu đã vote up và tiếp tục nhấn vote up
					vote.isDownvoted() && voteType.equals(VoteType.DOWN)) { //nếu đã vote down và tiếp tục nhấn vote down
				return undoVote(vote, reviewId, voteType);
			} else if (vote.isUpvoted() && voteType.equals(VoteType.DOWN)) {//nếu đã vote up và nhấn vote down
				vote.voteDown();//thay đổi vote trong db bằng vote down
			} else if (vote.isDownvoted() && voteType.equals(VoteType.UP)) {//nếu đã vote down và nhấn vote up
				vote.voteUp();//thay đổi vote trong db bằng vote up
			}
		} else {
			vote = new ReviewVote();//nếu chưa vote lần nào thì tạo đối tượng reviewVote
			vote.setCustomer(customer);//gán customer này thuộc về vote hiện tại
			vote.setReview(review);//gán review này thuộc về vote hiện tại
			
			if (voteType.equals(VoteType.UP)) {
				vote.voteUp();//nếu đang vote up thì gán votes = 1
			} else {
				vote.voteDown();//nếu đang vote down thì gán votes = -1
			}
		}
		
		voteRepo.save(vote);//lưu vote xuống db
		reviewRepo.updateVoteCount(reviewId);//update lại tổng số vote cho review này(có nghĩa là chỉ cần tính tổng số vote, ko quan tâm vote up hay vote down)
		Integer voteCount = reviewRepo.getVoteCount(reviewId);//lấy ra tổng số vote cho review này
		
		return VoteResult.success("You have successfully voted " + voteType + " that review.", 
				voteCount);
	}
	
	public void markReviewsVotedForProductByCustomer(List<Review> listReviews, Integer productId,
			Integer customerId) {
		List<ReviewVote> listVotes = voteRepo.findByProductAndCustomer(productId, customerId);
		
		for (ReviewVote vote : listVotes) {//lấy ra tất cả votes của customer hiện tại cho product này
			Review votedReview = vote.getReview();//lấy ra review của customer hiện tại từ mỗi vote
			
			if (listReviews.contains(votedReview)) {
				int index = listReviews.indexOf(votedReview);
				Review review = listReviews.get(index);//nếu 3 reviews vừa lấy từ db có chứa review của customer cho product hiện tại -->gán vote cho review này
				
				if (vote.isUpvoted()) {
					review.setUpvotedByCurrentCustomer(true);//nếu customer hiện tại vote up thì sẽ in đậm thumb up -->các customer khác sẽ ko thấy in đậm thumb up
				} else if (vote.isDownvoted()) {
					review.setDownvotedByCurrentCustomer(true);//nếu customer hiện tại vote down thì sẽ in đậm thumb down -->các customer khác sẽ ko thấy in đậm thumb down
				}
			}
		}
	}
}
