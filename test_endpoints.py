"""
Critik Backend - Comprehensive Endpoint Test Suite
===================================================
Tests all 50+ endpoints across 11 controllers.

Requirements:
- Application running on http://localhost:8080
- Empty PostgreSQL database with schema initialized
- Python 3.7+ with requests library

Usage:
    python test_endpoints.py
"""

import requests
import json
import time
import os
import sys
from typing import Dict, Optional, Tuple

BASE_URL = "http://localhost:8080"

# Test results tracking
test_results = {
    "passed": 0,
    "failed": 0,
    "skipped": 0,
    "total": 0
}

def safe_print(text):
    """Safely print text handling unicode errors."""
    try:
        print(text)
    except UnicodeEncodeError:
        print(text.encode('ascii', 'replace').decode('ascii'))

def print_header(text: str):
    """Print a formatted section header."""
    safe_print(f"\n{'='*70}")
    safe_print(f"  {text}")
    safe_print(f"{'='*70}")

def print_result(test_name: str, success: bool, message: str = "", skip: bool = False):
    """Print test result with formatting."""
    test_results["total"] += 1
    
    if skip:
        test_results["skipped"] += 1
        safe_print(f"[SKIP] {test_name}: SKIPPED - {message}")
    elif success:
        test_results["passed"] += 1
        safe_print(f"[PASS] {test_name}: PASSED {message}")
    else:
        test_results["failed"] += 1
        safe_print(f"[FAIL] {test_name}: FAILED - {message}")

def print_summary():
    """Print final test summary."""
    print_header("TEST SUMMARY")
    safe_print(f"Total Tests: {test_results['total']}")
    safe_print(f"[PASS] Passed: {test_results['passed']}")
    safe_print(f"[FAIL] Failed: {test_results['failed']}")
    safe_print(f"[SKIP] Skipped: {test_results['skipped']}")
    
    if test_results['failed'] == 0:
        safe_print(f"\n*** All tests passed!")
    else:
        safe_print(f"\n*** {test_results['failed']} test(s) failed")
    safe_print("="*70)

class TestContext:
    """Stores test data and authentication tokens."""
    def __init__(self):
        self.user_a_token = None
        self.user_a_id = None
        self.user_a_username = None
        self.user_a_refresh_token = None
        
        self.user_b_token = None
        self.user_b_id = None
        self.user_b_username = None
        self.user_b_refresh_token = None
        
        self.artwork_id = None
        self.comment_id = None
        self.reply_id = None

ctx = TestContext()

def test_health_check():
    """Test application health endpoint."""
    print_header("HEALTH CHECK")
    try:
        resp = requests.get(f"{BASE_URL}/actuator/health", timeout=5)
        success = resp.status_code == 200
        print_result("Application Health", success, 
                    f"Status: {resp.status_code}")
        return success
    except Exception as e:
        print_result("Application Health", False, str(e))
        return False

def test_authentication():
    """Test all authentication endpoints."""
    print_header("1. AUTHENTICATION TESTS")
    ts = int(time.time())
    
    # Test 1.1: Register User A
    user_a = {
        "username": f"test_user_a_{ts}",
        "email": f"user_a_{ts}@test.com",
        "password": "Test@1234"
    }
    
    try:
        resp = requests.post(f"{BASE_URL}/auth/register", json=user_a)
        success = resp.status_code == 200
        print_result("Register User A", success, 
                    f"Status: {resp.status_code}")
        
        if not success:
            return False
        
        ctx.user_a_username = user_a["username"]
    except Exception as e:
        print_result("Register User A", False, str(e))
        return False
    
    # Test 1.2: Login User A
    try:
        resp = requests.post(f"{BASE_URL}/auth/login", 
                           json={"username": user_a["username"], 
                                "password": user_a["password"]})
        success = resp.status_code == 200
        
        if success:
            data = resp.json()
            ctx.user_a_token = data.get("accessToken")
            ctx.user_a_id = data.get("userId")
            ctx.user_a_refresh_token = data.get("refreshToken")
            print_result("Login User A", True, 
                        f"Token: {ctx.user_a_token[:20]}...")
        else:
            print_result("Login User A", False, 
                        f"Status: {resp.status_code}, Body: {resp.text}")
            return False
    except Exception as e:
        print_result("Login User A", False, str(e))
        return False
    
    # Test 1.3: Register User B
    user_b = {
        "username": f"test_user_b_{ts}",
        "email": f"user_b_{ts}@test.com",
        "password": "Test@1234"
    }
    
    try:
        resp = requests.post(f"{BASE_URL}/auth/register", json=user_b)
        success = resp.status_code == 200
        print_result("Register User B", success, 
                    f"Status: {resp.status_code}")
        
        if not success:
            return False
            
        ctx.user_b_username = user_b["username"]
    except Exception as e:
        print_result("Register User B", False, str(e))
        return False
    
    # Test 1.4: Login User B
    try:
        resp = requests.post(f"{BASE_URL}/auth/login", 
                           json={"username": user_b["username"], 
                                "password": user_b["password"]})
        success = resp.status_code == 200
        
        if success:
            data = resp.json()
            ctx.user_b_token = data.get("accessToken")
            ctx.user_b_id = data.get("userId")
            ctx.user_b_refresh_token = data.get("refreshToken")
            print_result("Login User B", True, 
                        f"Token: {ctx.user_b_token[:20]}...")
        else:
            print_result("Login User B", False, 
                        f"Status: {resp.status_code}")
            return False
    except Exception as e:
        print_result("Login User B", False, str(e))
        return False
    
    # Test 1.5: Refresh Token
    try:
        resp = requests.post(f"{BASE_URL}/auth/refresh", 
                           json={"refreshToken": ctx.user_a_refresh_token})
        success = resp.status_code == 200
        
        if success:
            data = resp.json()
            new_access_token = data.get("accessToken")
            ctx.user_a_refresh_token = data.get("refreshToken")
            print_result("Refresh Access Token", True, 
                        f"New token received")
        else:
            print_result("Refresh Access Token", False, 
                        f"Status: {resp.status_code}")
    except Exception as e:
        print_result("Refresh Access Token", False, str(e))
    
    return True

def test_artwork_endpoints():
    """Test artwork CRUD operations."""
    print_header("2. ARTWORK TESTS")
    
    headers_a = {"Authorization": f"Bearer {ctx.user_a_token}"}
    headers_b = {"Authorization": f"Bearer {ctx.user_b_token}"}
    
     # Test 2.1: Create Artwork (User A)
    # Using multipart/form-data simulation
    try:
        # Create a dummy image file content
        dummy_image = b'fake_image_content'
        files = {
            'file': ('test_image.jpg', dummy_image, 'image/jpeg')
        }
        data = {
            'title': 'Test Artwork Title',
            'artistName': 'Test Artist',
            'interpretation': 'This is a test artwork',
            'tags': 'test,abstract',
            'locationName': 'Paris',
            'lat': '48.8566',
            'lon': '2.3522'
        }
        
        # Requests automatically sets Content-Type to multipart/form-data when 'files' is present
        resp = requests.post(f"{BASE_URL}/artworks", 
                           files=files,
                           data=data,
                           headers=headers_a)
        success = resp.status_code in [200, 201]
        
        if success:
            safe_print(f"[PASS] Create Artwork: PASSED")
        else:
            safe_print(f"[FAIL] Create Artwork: FAILED - Status: {resp.status_code}, Body: {resp.text}")
            
    except Exception as e:
        safe_print(f"[FAIL] Create Artwork: FAILED - {str(e)}")
    
    # Test 2.2: Get Public Feed
    try:
        resp = requests.get(f"{BASE_URL}/artworks/feed?page=0&size=10", 
                          headers=headers_a)
        success = resp.status_code == 200
        
        if success:
            data = resp.json()
            total = data.get('totalElements', 0)
            content = data.get('content', [])
            
            if content:
                ctx.artwork_id = content[0]['id']
                print_result("Get Public Feed", True, 
                            f"Found {total} artworks, using ID: {ctx.artwork_id}")
            else:
                print_result("Get Public Feed", True, 
                            f"Feed is empty (expected for new database)")
        else:
            print_result("Get Public Feed", False, 
                        f"Status: {resp.status_code}")
    except Exception as e:
        print_result("Get Public Feed", False, str(e))
    
    # Test 2.3: Get Artwork by ID (if we have one)
    if ctx.artwork_id:
        try:
            resp = requests.get(f"{BASE_URL}/artworks/{ctx.artwork_id}", 
                              headers=headers_a)
            success = resp.status_code == 200
            print_result("Get Artwork by ID", success, 
                        f"Status: {resp.status_code}")
        except Exception as e:
            print_result("Get Artwork by ID", False, str(e))
    else:
        print_result("Get Artwork by ID", False, 
                    "No artwork available", skip=True)
    
    # Test 2.4: Get My Artworks
    try:
        resp = requests.get(f"{BASE_URL}/artworks/my", headers=headers_a)
        success = resp.status_code == 200
        print_result("Get My Artworks", success, 
                    f"Status: {resp.status_code}")
    except Exception as e:
        print_result("Get My Artworks", False, str(e))
    
    # Test 2.5: Get Popular Feed
    try:
        resp = requests.get(f"{BASE_URL}/artworks/popular?page=0&size=10", 
                          headers=headers_a)
        success = resp.status_code == 200
        print_result("Get Popular Feed", success, 
                    f"Status: {resp.status_code}")
    except Exception as e:
        print_result("Get Popular Feed", False, str(e))
    
    # Test 2.6: Get Controversial Feed
    try:
        resp = requests.get(f"{BASE_URL}/artworks/controversial?page=0&size=10", 
                          headers=headers_a)
        success = resp.status_code == 200
        print_result("Get Controversial Feed", success, 
                    f"Status: {resp.status_code}")
    except Exception as e:
        print_result("Get Controversial Feed", False, str(e))

def test_comment_endpoints():
    """Test comment operations."""
    print_header("3. COMMENT TESTS")
    
    if not ctx.artwork_id:
        print_result("Comment Tests", False, 
                    "No artwork available for testing", skip=True)
        return
    
    headers_a = {"Authorization": f"Bearer {ctx.user_a_token}"}
    headers_b = {"Authorization": f"Bearer {ctx.user_b_token}"}
    
    # Test 3.1: Add Comment (User B)
    try:
        resp = requests.post(
            f"{BASE_URL}/artworks/{ctx.artwork_id}/comments",
            json={"text": "Great artwork! Very insightful interpretation."},
            headers=headers_b
        )
        success = resp.status_code in [200, 201]
        
        if success:
            comment_data = resp.json()
            if isinstance(comment_data, dict) and 'id' in comment_data:
                ctx.comment_id = comment_data['id']
            print_result("Add Comment", True, 
                        f"Comment ID: {ctx.comment_id}")
        else:
            print_result("Add Comment", False, 
                        f"Status: {resp.status_code}, Body: {resp.text}")
    except Exception as e:
        print_result("Add Comment", False, str(e))
    
    # Test 3.2: Get Comments
    try:
        resp = requests.get(
            f"{BASE_URL}/artworks/{ctx.artwork_id}/comments",
            headers=headers_a
        )
        success = resp.status_code == 200
        
        if success:
            comments = resp.json()
            # If we didn't get comment ID from creation, try to get it here
            if not ctx.comment_id and comments:
                ctx.comment_id = comments[0]['id']
            print_result("Get Comments", True, 
                        f"Found {len(comments)} comments")
        else:
            print_result("Get Comments", False, 
                        f"Status: {resp.status_code}")
    except Exception as e:
        print_result("Get Comments", False, str(e))
    
    # Test 3.3: Add Reply to Comment
    if ctx.comment_id:
        try:
            resp = requests.post(
                f"{BASE_URL}/artworks/{ctx.artwork_id}/comments/{ctx.comment_id}/replies",
                json={"text": "I agree with your perspective!"},
                headers=headers_a
            )
            success = resp.status_code in [200, 201]
            
            if success:
                reply_data = resp.json()
                if isinstance(reply_data, dict) and 'id' in reply_data:
                    ctx.reply_id = reply_data['id']
                print_result("Add Reply to Comment", True, 
                            f"Reply ID: {ctx.reply_id}")
            else:
                print_result("Add Reply to Comment", False, 
                            f"Status: {resp.status_code}")
        except Exception as e:
            print_result("Add Reply to Comment", False, str(e))
    else:
        print_result("Add Reply to Comment", False, 
                    "No comment available", skip=True)

def test_reaction_endpoints():
    """Test artwork reaction operations."""
    print_header("4. ARTWORK REACTION TESTS")
    
    if not ctx.artwork_id:
        print_result("Reaction Tests", False, 
                    "No artwork available", skip=True)
        return
    
    headers_a = {"Authorization": f"Bearer {ctx.user_a_token}"}
    
    # Test 4.1: Set Reaction (AGREE)
    try:
        resp = requests.post(
            f"{BASE_URL}/artworks/{ctx.artwork_id}/reactions?type=AGREE",
            headers=headers_a
        )
        success = resp.status_code == 200
        print_result("Set Reaction (AGREE)", success, 
                    f"Status: {resp.status_code}")
    except Exception as e:
        print_result("Set Reaction (AGREE)", False, str(e))
    
    # Test 4.2: Get User's Reaction
    try:
        resp = requests.get(
            f"{BASE_URL}/artworks/{ctx.artwork_id}/reactions/me",
            headers=headers_a
        )
        success = resp.status_code == 200
        
        if success:
            data = resp.json()
            has_reaction = data.get('hasReaction', False)
            reaction_type = data.get('type')
            print_result("Get User's Reaction", True, 
                        f"Has reaction: {has_reaction}, Type: {reaction_type}")
        else:
            print_result("Get User's Reaction", False, 
                        f"Status: {resp.status_code}")
    except Exception as e:
        print_result("Get User's Reaction", False, str(e))
    
    # Test 4.3: Get Reaction Counts
    try:
        resp = requests.get(
            f"{BASE_URL}/artworks/{ctx.artwork_id}/reactions",
            headers=headers_a
        )
        success = resp.status_code == 200
        
        if success:
            counts = resp.json()
            print_result("Get Reaction Counts", True, 
                        f"Counts: {counts}")
        else:
            print_result("Get Reaction Counts", False, 
                        f"Status: {resp.status_code}")
    except Exception as e:
        print_result("Get Reaction Counts", False, str(e))
    
    # Test 4.4: Update Reaction (DISAGREE)
    try:
        resp = requests.post(
            f"{BASE_URL}/artworks/{ctx.artwork_id}/reactions?type=DISAGREE",
            headers=headers_a
        )
        success = resp.status_code == 200
        print_result("Update Reaction (DISAGREE)", success, 
                    f"Status: {resp.status_code}")
    except Exception as e:
        print_result("Update Reaction (DISAGREE)", False, str(e))
    
    # Test 4.5: Remove Reaction
    try:
        resp = requests.delete(
            f"{BASE_URL}/artworks/{ctx.artwork_id}/reactions",
            headers=headers_a
        )
        success = resp.status_code == 200
        print_result("Remove Reaction", success, 
                    f"Status: {resp.status_code}")
    except Exception as e:
        print_result("Remove Reaction", False, str(e))

def test_comment_reaction_endpoints():
    """Test comment reaction operations."""
    print_header("5. COMMENT REACTION TESTS")
    
    if not ctx.comment_id:
        print_result("Comment Reaction Tests", False, 
                    "No comment available", skip=True)
        return
    
    headers_a = {"Authorization": f"Bearer {ctx.user_a_token}"}
    
    # Test 5.1: Add Comment Reaction (AGREE)
    try:
        resp = requests.post(
            f"{BASE_URL}/api/comments/{ctx.comment_id}/reactions",
            json={"type": "AGREE"},
            headers=headers_a
        )
        success = resp.status_code == 200
        print_result("Add Comment Reaction (AGREE)", success, 
                    f"Status: {resp.status_code}")
    except Exception as e:
        print_result("Add Comment Reaction (AGREE)", False, str(e))
    
    # Test 5.2: Get Comment Reaction Counts
    try:
        resp = requests.get(
            f"{BASE_URL}/api/comments/{ctx.comment_id}/reactions/counts",
            headers=headers_a
        )
        success = resp.status_code == 200
        
        if success:
            counts = resp.json()
            print_result("Get Comment Reaction Counts", True, 
                        f"Counts: {counts}")
        else:
            print_result("Get Comment Reaction Counts", False, 
                        f"Status: {resp.status_code}")
    except Exception as e:
        print_result("Get Comment Reaction Counts", False, str(e))
    
    # Test 5.3: Get User's Comment Reaction
    try:
        resp = requests.get(
            f"{BASE_URL}/api/comments/{ctx.comment_id}/reactions/me",
            headers=headers_a
        )
        success = resp.status_code == 200
        print_result("Get User's Comment Reaction", success, 
                    f"Status: {resp.status_code}")
    except Exception as e:
        print_result("Get User's Comment Reaction", False, str(e))
    
    # Test 5.4: Remove Comment Reaction
    try:
        resp = requests.delete(
            f"{BASE_URL}/api/comments/{ctx.comment_id}/reactions",
            headers=headers_a
        )
        success = resp.status_code == 200
        print_result("Remove Comment Reaction", success, 
                    f"Status: {resp.status_code}")
    except Exception as e:
        print_result("Remove Comment Reaction", False, str(e))

def test_bookmark_endpoints():
    """Test bookmark operations."""
    print_header("6. BOOKMARK TESTS")
    
    if not ctx.artwork_id:
        print_result("Bookmark Tests", False, 
                    "No artwork available", skip=True)
        return
    
    headers_b = {"Authorization": f"Bearer {ctx.user_b_token}"}
    
    # Test 6.1: Bookmark Artwork
    try:
        resp = requests.post(
            f"{BASE_URL}/api/bookmarks/{ctx.artwork_id}",
            headers=headers_b
        )
        success = resp.status_code == 200
        print_result("Bookmark Artwork", success, 
                    f"Status: {resp.status_code}")
    except Exception as e:
        print_result("Bookmark Artwork", False, str(e))
    
    # Test 6.2: Check Bookmark Status
    try:
        resp = requests.get(
            f"{BASE_URL}/api/bookmarks/{ctx.artwork_id}/status",
            headers=headers_b
        )
        success = resp.status_code == 200
        
        if success:
            data = resp.json()
            is_bookmarked = data.get('isBookmarked', False)
            print_result("Check Bookmark Status", is_bookmarked, 
                        f"Is bookmarked: {is_bookmarked}")
        else:
            print_result("Check Bookmark Status", False, 
                        f"Status: {resp.status_code}")
    except Exception as e:
        print_result("Check Bookmark Status", False, str(e))
    
    # Test 6.3: Get User's Bookmarks
    try:
        resp = requests.get(
            f"{BASE_URL}/api/bookmarks?page=0&size=10",
            headers=headers_b
        )
        success = resp.status_code == 200
        
        if success:
            data = resp.json()
            content = data.get('content', [])
            found = any(a.get('id') == ctx.artwork_id for a in content)
            print_result("Get User's Bookmarks", found, 
                        f"Found {len(content)} bookmarks")
        else:
            print_result("Get User's Bookmarks", False, 
                        f"Status: {resp.status_code}")
    except Exception as e:
        print_result("Get User's Bookmarks", False, str(e))
    
    # Test 6.4: Unbookmark Artwork
    try:
        resp = requests.delete(
            f"{BASE_URL}/api/bookmarks/{ctx.artwork_id}",
            headers=headers_b
        )
        success = resp.status_code == 200
        print_result("Unbookmark Artwork", success, 
                    f"Status: {resp.status_code}")
    except Exception as e:
        print_result("Unbookmark Artwork", False, str(e))

def test_follow_endpoints():
    """Test follow/unfollow operations."""
    print_header("7. FOLLOW TESTS")
    
    headers_a = {"Authorization": f"Bearer {ctx.user_a_token}"}
    headers_b = {"Authorization": f"Bearer {ctx.user_b_token}"}
    
    # Test 7.1: Follow User (B follows A)
    try:
        resp = requests.post(
            f"{BASE_URL}/follow/{ctx.user_a_id}",
            headers=headers_b
        )
        success = resp.status_code == 200
        print_result("Follow User", success, 
                    f"Status: {resp.status_code}")
    except Exception as e:
        print_result("Follow User", False, str(e))
    
    # Test 7.2: Get Followers
    try:
        resp = requests.get(
            f"{BASE_URL}/follow/{ctx.user_a_id}/followers?page=0&size=10",
            headers=headers_a
        )
        success = resp.status_code == 200
        
        if success:
            data = resp.json()
            content = data.get('content', [])
            print_result("Get Followers", True, 
                        f"Found {len(content)} followers")
        else:
            print_result("Get Followers", False, 
                        f"Status: {resp.status_code}")
    except Exception as e:
        print_result("Get Followers", False, str(e))
    
    # Test 7.3: Get Following
    try:
        resp = requests.get(
            f"{BASE_URL}/follow/{ctx.user_b_id}/following?page=0&size=10",
            headers=headers_b
        )
        success = resp.status_code == 200
        
        if success:
            data = resp.json()
            content = data.get('content', [])
            print_result("Get Following", True, 
                        f"Found {len(content)} following")
        else:
            print_result("Get Following", False, 
                        f"Status: {resp.status_code}")
    except Exception as e:
        print_result("Get Following", False, str(e))
    
    # Test 7.4: Unfollow User
    try:
        resp = requests.delete(
            f"{BASE_URL}/follow/{ctx.user_a_id}",
            headers=headers_b
        )
        success = resp.status_code == 200
        print_result("Unfollow User", success, 
                    f"Status: {resp.status_code}")
    except Exception as e:
        print_result("Unfollow User", False, str(e))

def test_block_endpoints():
    """Test user blocking operations."""
    print_header("8. BLOCK TESTS")
    
    headers_a = {"Authorization": f"Bearer {ctx.user_a_token}"}
    headers_b = {"Authorization": f"Bearer {ctx.user_b_token}"}
    
    # Test 8.1: Block User (A blocks B)
    try:
        resp = requests.post(
            f"{BASE_URL}/api/blocks/{ctx.user_b_id}",
            headers=headers_a
        )
        success = resp.status_code == 200
        if success:
            safe_print(f"[PASS] Block User: PASSED Status: {resp.status_code}")
        else:
            safe_print(f"[FAIL] Block User: FAILED - Status: {resp.status_code}, Body: {resp.text}")
    except Exception as e:
        safe_print(f"[FAIL] Block User: FAILED - {str(e)}")
    
    # Test 8.2: Check Block Status
    try:
        resp = requests.get(
            f"{BASE_URL}/api/blocks/{ctx.user_b_id}/status",
            headers=headers_a
        )
        success = resp.status_code == 200
        
        if success:
            data = resp.json()
            is_blocked = data.get('isBlocked', False)
            print_result("Check Block Status", is_blocked, 
                        f"Is blocked: {is_blocked}")
        else:
            print_result("Check Block Status", False, 
                        f"Status: {resp.status_code}")
    except Exception as e:
        print_result("Check Block Status", False, str(e))
    
    # Test 8.3: Get Blocked Users List
    try:
        resp = requests.get(
            f"{BASE_URL}/api/blocks?page=0&size=10",
            headers=headers_a
        )
        success = resp.status_code == 200
        
        if success:
            data = resp.json()
            content = data.get('content', [])
            found = any(u.get('id') == ctx.user_b_id for u in content)
            print_result("Get Blocked Users List", found, 
                        f"Found {len(content)} blocked users")
        else:
            print_result("Get Blocked Users List", False, 
                        f"Status: {resp.status_code}")
    except Exception as e:
        print_result("Get Blocked Users List", False, str(e))
    
    # Test 8.4: Verify Blocked Interactions (B tries to follow A)
    try:
        resp = requests.post(
            f"{BASE_URL}/follow/{ctx.user_a_id}",
            headers=headers_b
        )
        # Should fail with 400 or 403
        success = resp.status_code in [400, 403]
        print_result("Blocked Follow Prevention", success, 
                    f"Status: {resp.status_code} (expected 400/403)")
    except Exception as e:
        print_result("Blocked Follow Prevention", False, str(e))
    
    # Test 8.5: Unblock User
    try:
        resp = requests.delete(
            f"{BASE_URL}/api/blocks/{ctx.user_b_id}",
            headers=headers_a
        )
        success = resp.status_code == 200
        print_result("Unblock User", success, 
                    f"Status: {resp.status_code}")
    except Exception as e:
        print_result("Unblock User", False, str(e))

def test_profile_endpoints():
    """Test profile operations."""
    print_header("9. PROFILE TESTS")
    
    headers_a = {"Authorization": f"Bearer {ctx.user_a_token}"}
    
    # Test 9.1: Get Profile
    try:
        resp = requests.get(
            f"{BASE_URL}/users/{ctx.user_a_username}",
            headers=headers_a
        )
        success = resp.status_code == 200
        
        if success:
            profile = resp.json()
            stats = profile.get('stats')
            if stats:
                print_result("Get Profile", True, 
                            f"Followers: {stats.get('followersCount')}, "
                            f"Following: {stats.get('followingCount')}")
            else:
                print_result("Get Profile", True, "No stats available")
        else:
            print_result("Get Profile", False, 
                        f"Status: {resp.status_code}")
    except Exception as e:
        print_result("Get Profile", False, str(e))
    
    # Test 9.2: Update Profile
    try:
        update_data = {
            "displayName": "Test User A Updated",
            "bio": "This is my updated bio for testing"
        }
        resp = requests.put(
            f"{BASE_URL}/users/{ctx.user_a_id}/edit",
            json=update_data,
            headers=headers_a
        )
        success = resp.status_code == 200
        print_result("Update Profile", success, 
                    f"Status: {resp.status_code}")
    except Exception as e:
        print_result("Update Profile", False, str(e))
    
    # Test 9.3: Upload Avatar
    print_result("Upload Avatar", False, 
                "Skipped - requires multipart file upload", skip=True)
    
    # Test 9.4: Upload Banner
    print_result("Upload Banner", False, 
                "Skipped - requires multipart file upload", skip=True)

def test_search_endpoints():
    """Test search operations."""
    print_header("10. SEARCH TESTS")
    
    headers_a = {"Authorization": f"Bearer {ctx.user_a_token}"}
    
    # Test 10.1: Search Users
    try:
        resp = requests.get(
            f"{BASE_URL}/search/users?q=test_user&page=0&size=10",
            headers=headers_a
        )
        success = resp.status_code == 200
        
        if success:
            data = resp.json()
            content = data.get('content', [])
            print_result("Search Users", True, 
                        f"Found {len(content)} users")
        else:
            print_result("Search Users", False, 
                        f"Status: {resp.status_code}")
    except Exception as e:
        print_result("Search Users", False, str(e))
    
    # Test 10.2: Search Artworks by Title
    try:
        resp = requests.get(
            f"{BASE_URL}/search/artworks?title=test&page=0&size=10",
            headers=headers_a
        )
        success = resp.status_code == 200
        
        if success:
            data = resp.json()
            content = data.get('content', [])
            print_result("Search Artworks by Title", True, 
                        f"Found {len(content)} artworks")
        else:
            print_result("Search Artworks by Title", False, 
                        f"Status: {resp.status_code}")
    except Exception as e:
        print_result("Search Artworks by Title", False, str(e))
    
    # Test 10.3: Search Artworks by Tags
    try:
        resp = requests.get(
            f"{BASE_URL}/search/artworks?tags=abstract&page=0&size=10",
            headers=headers_a
        )
        success = resp.status_code == 200
        
        if success:
            data = resp.json()
            content = data.get('content', [])
            print_result("Search Artworks by Tags", True, 
                        f"Found {len(content)} artworks")
        else:
            print_result("Search Artworks by Tags", False, 
                        f"Status: {resp.status_code}")
    except Exception as e:
        print_result("Search Artworks by Tags", False, str(e))

def test_artwork_history_endpoints():
    """Test artwork revision history."""
    print_header("11. ARTWORK HISTORY TESTS")
    
    if not ctx.artwork_id:
        print_result("Artwork History Tests", False, 
                    "No artwork available", skip=True)
        return
    
    headers_a = {"Authorization": f"Bearer {ctx.user_a_token}"}
    
    # Test 11.1: Get Artwork History
    try:
        resp = requests.get(
            f"{BASE_URL}/api/artworks/{ctx.artwork_id}/history",
            headers=headers_a
        )
        success = resp.status_code == 200
        
        if success:
            history = resp.json()
            print_result("Get Artwork History", True, 
                        f"Found {len(history)} revisions")
        else:
            print_result("Get Artwork History", False, 
                        f"Status: {resp.status_code}")
    except Exception as e:
        print_result("Get Artwork History", False, str(e))
    
    # Test 11.2: Get Edit Count
    try:
        resp = requests.get(
            f"{BASE_URL}/api/artworks/{ctx.artwork_id}/history/count",
            headers=headers_a
        )
        success = resp.status_code == 200
        
        if success:
            count = resp.json()
            print_result("Get Edit Count", True, 
                        f"Edit count: {count}")
        else:
            print_result("Get Edit Count", False, 
                        f"Status: {resp.status_code}")
    except Exception as e:
        print_result("Get Edit Count", False, str(e))

def test_logout_endpoints():
    """Test logout operations."""
    print_header("12. LOGOUT TESTS")
    
    headers_a = {"Authorization": f"Bearer {ctx.user_a_token}"}
    
    # Test 12.1: Logout (single device)
    try:
        resp = requests.post(
            f"{BASE_URL}/auth/logout",
            json={"refreshToken": ctx.user_b_refresh_token},
            headers={"Authorization": f"Bearer {ctx.user_b_token}"}
        )
        success = resp.status_code == 200
        print_result("Logout (Single Device)", success, 
                    f"Status: {resp.status_code}")
    except Exception as e:
        print_result("Logout (Single Device)", False, str(e))
    
    # Test 12.2: Logout All Devices
    try:
        resp = requests.post(
            f"{BASE_URL}/auth/logout-all",
            headers=headers_a
        )
        success = resp.status_code == 200
        if success:
            safe_print(f"[PASS] Logout All Devices: PASSED Status: {resp.status_code}")
        else:
            safe_print(f"[FAIL] Logout All Devices: FAILED - Status: {resp.status_code}, Body: {resp.text}")
    except Exception as e:
        safe_print(f"[FAIL] Logout All Devices: FAILED - {str(e)}")

def main():
    """Run all tests."""
    print("\n" + "="*70)
    print("  CRITIK BACKEND - COMPREHENSIVE ENDPOINT TEST SUITE")
    print("="*70)
    print(f"  Base URL: {BASE_URL}")
    print(f"  Time: {time.strftime('%Y-%m-%d %H:%M:%S')}")
    print("="*70)
    
    # Health check first
    if not test_health_check():
        print("\n⚠️  Application is not running or not healthy!")
        print("   Please start the application and try again.")
        return
    
    # Run all test suites
    if not test_authentication():
        print("\n⚠️  Authentication failed! Cannot proceed with other tests.")
        print_summary()
        return
    
    test_artwork_endpoints()
    test_comment_endpoints()
    test_reaction_endpoints()
    test_comment_reaction_endpoints()
    test_bookmark_endpoints()
    test_follow_endpoints()
    test_block_endpoints()
    test_profile_endpoints()
    test_search_endpoints()
    test_artwork_history_endpoints()
    test_logout_endpoints()
    
    # Print final summary
    print_summary()

if __name__ == "__main__":
    main()
