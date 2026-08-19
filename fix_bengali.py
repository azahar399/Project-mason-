import os
import re

replacements = {
    "Search flat, mason, area, status... (সার্চ করুন)": "Search flat, mason, area, status...",
    "Bold (গাঢ়)": "Bold",
    "Italic (বাঁকা)": "Italic",
    "Fill Color (ব্যাকগ্রাউন্ড কালার)": "Fill Color",
    "Text Color (লেখার কালার)": "Text Color",
    "বেতনের হিসাব (মাস্টার) / Wage Ledger": "Wage Ledger",
    "দৈনিক খতিয়ান / Daily Sheet": "Daily Sheet",
    "পিডিএফ রিপোর্ট শেয়ার (PDF Share)": "PDF Share",
    "এক্সেল রিপোর্ট শেয়ার (CSV Share)": "CSV Share",
    "মোট উপার্জন / Earned": "Total Earned",
    "মোট অ্যাডভান্স / Advance": "Total Advance",
    "বকেয়া / Due Balance": "Due Balance",
    "মিস্ত্রি / Masons": "Masons",
    "হেল্পার / Helpers": "Helpers",
    "কোনো কর্মী পাওয়া যায়নি!\\nNo personnel found under this tab.": "No personnel found under this tab.",
    "দিন": "Day",
    "মোট হাজিরা / Attendance": "Total Attendance",
    "দিন / Days": "Days",
    "মোট পাওনা / Earned": "Total Earned",
    "অগ্রিম / Advance": "Advance",
    "অবশিষ্ট বকেয়া / Net Balance (Due)": "Net Balance (Due)",
    "অতিরিক্ত প্রদান / Overpaid": "Overpaid",
    "হাজিরা রেট পরিবর্তন / Edit Rate": "Edit Rate",
    "কর্মী: ": "Personnel: ",
    "দৈনিক হাজিরা রেট / Daily Rate (৳)": "Daily Rate (৳)",
    "সংরক্ষণ করুন / Save": "Save",
    "বাতিল / Cancel": "Cancel",
    " - বিস্তারিত খতিয়ান / Detailed Ledger": " - Detailed Ledger",
    "মোট আয়": "Total Earned",
    "অগ্রিম": "Advance",
    "পুরস্কার": "Reward",
    "জরিমানা": "Penalty",
    "লেনদেনের বিবরণ / Logs": "Logs",
    "কোনো রেকর্ড নেই / No records found": "No records found",
    "হাজিরা: ": "Attendance: ",
    "আয়: ৳": "Income: ৳",
    "অগ্রিম: ৳": "Advance: ৳",
    "পুরস্কার: ৳": "Reward: ৳",
    "জরিমানা: ৳": "Penalty: ৳",
    "Name (নাম),Rate (রেট),Attendance Unit (হাজিরার ইউনিট),Total Income (মোট উপার্জন),Advance Taken (অগ্রিম গ্রহণ),Reward (পুরস্কার),Penalty (জরিমানা),Due Balance (বকেয়া পাওনা)": "Name,Rate,Attendance Unit,Total Income,Advance Taken,Reward,Penalty,Due Balance",
    " // Search Option Bar (সার্চ অপশন)": " // Search Option Bar",
}

for root, dirs, files in os.walk('app/src/main/java'):
    for file in files:
        if file.endswith('.kt'):
            filepath = os.path.join(root, file)
            with open(filepath, 'r') as f:
                content = f.read()
            
            original_content = content
            for old, new in replacements.items():
                content = content.replace(old, new)
                
            if content != original_content:
                with open(filepath, 'w') as f:
                    f.write(content)
                print(f"Updated {filepath}")
