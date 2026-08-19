import os
import re

replacements = {
    "ইতিমধ্যে এন্ট্রি আছে! / Entry Exists!": "Entry Exists!",
    "এই ফ্ল্যাট এবং কাজের জন্য ইতিমধ্যে একটি এন্ট্রি রয়েছে। আপনি কি আগের এন্ট্রিটি পরিবর্তন করতে চান?": "An entry already exists for this flat and work column. Do you want to overwrite it?",
    "হ্যাঁ, পরিবর্তন করুন / Yes, Overwrite": "Yes, Overwrite",
    "বাতিল করুন / Cancel": "Cancel",
    "বেতনের খতিয়ান / Wage Ledger": "Wage Ledger",
    "Name (নাম),Rate (রেট),Attendance Unit (হাজিরার ইউনিট),Total Income (মোট উপার্জন),Advance Taken (Advance গ্রহণ),Reward (Reward),Penalty (Penalty),Due Balance (বকেয়া পাওনা)": "Name,Rate,Attendance Unit,Total Income,Advance Taken,Reward,Penalty,Due Balance",
    "৳": "Tk ",
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
