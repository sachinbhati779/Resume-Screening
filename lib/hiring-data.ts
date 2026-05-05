import {
  BadgeCheck,
  Bot,
  BriefcaseBusiness,
  FileCheck2,
  ListChecks,
  Trophy,
  UsersRound,
} from "lucide-react";

export const navItems = [
  {
    label: "Dashboard",
    href: "/dashboard",
    icon: BriefcaseBusiness,
  },
  {
    label: "Add Job Role",
    href: "/jobs/new",
    icon: ListChecks,
  },
  {
    label: "Upload Resume",
    href: "/resumes/upload",
    icon: FileCheck2,
  },
  {
    label: "Screening Results",
    href: "/results",
    icon: BadgeCheck,
  },
  {
    label: "Candidate Ranking",
    href: "/ranking",
    icon: Trophy,
  },
  {
    label: "AI Interview",
    href: "/interview",
    icon: Bot,
  },
  {
    label: "Shortlisted",
    href: "/shortlist",
    icon: UsersRound,
  },
];
